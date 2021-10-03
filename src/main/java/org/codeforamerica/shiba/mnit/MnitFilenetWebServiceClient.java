package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import com.sun.xml.messaging.saaj.soap.name.NameImpl;
import java.math.BigInteger;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.filenetwsdl.CmisContentStreamType;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertiesType;
import org.codeforamerica.shiba.filenetwsdl.CmisProperty;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyBoolean;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyId;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyString;
import org.codeforamerica.shiba.filenetwsdl.CreateDocument;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

@Component
@Slf4j
public class MnitFilenetWebServiceClient {

  private final WebServiceTemplate filenetWebServiceTemplate;
  private final Clock clock;
  private final String username;
  private final String password;
  private final CountyMap<CountyRoutingDestination> countyMap;
  private final ApplicationRepository applicationRepository;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public MnitFilenetWebServiceClient(@Qualifier("filenetWebServiceTemplate") WebServiceTemplate webServiceTemplate,
      Clock clock,
      @Value("${mnit-filenet.username}") String username,
      @Value("${mnit-filenet.password}") String password,
      CountyMap<CountyRoutingDestination> countyMap,
      ApplicationRepository applicationRepository) {
    this.filenetWebServiceTemplate = webServiceTemplate;
    this.clock = clock;
    this.username = username;
    this.password = password;
    this.countyMap = countyMap;
    this.applicationRepository = applicationRepository;
  }

  @Retryable(
      value = {Exception.class},
      maxAttemptsExpression = "#{${mnit-filenet.max-attempts}}",
      backoff = @Backoff(
          delayExpression = "#{${mnit-filenet.delay}}",
          multiplierExpression = "#{${mnit-filenet.multiplier}}",
          maxDelayExpression = "#{${mnit-filenet.max-delay}}"
      ),
      listeners = {"esbRetryListener"}
  )
  public void send(ApplicationFile applicationFile, RoutingDestination routingDestination, String applicationNumber,
      Document applicationDocument, FlowType flowType) {
    MDC.put("applicationFile", applicationFile.getFileName());
    CreateDocument createDocument = new CreateDocument();
    createDocument.setRepositoryId("Programs");
    setPropertiesOnDocument(applicationFile, routingDestination, createDocument);
    setContentStreamOnDocument(applicationFile, createDocument);

    filenetWebServiceTemplate.marshalSendAndReceive(createDocument, message -> {
      SOAPMessage soapMessage = ((SaajSoapMessage) message).getSaajMessage();
      try {
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        QName securityQName = new QName(
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
            "Security", "wsse");
        QName timestampQName = new QName(
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
            "Timestamp", "wsu");
        SOAPHeaderElement securityElement = soapHeader.addHeaderElement(securityQName);

        SOAPElement timestampElement = securityElement.addChildElement(timestampQName);
        SOAPElement createdElement = timestampElement.addChildElement("Created", "wsu");
        ZonedDateTime createdTimestamp = ZonedDateTime.now(clock);
        createdElement.setTextContent(createdTimestamp.format(DateTimeFormatter.ISO_INSTANT));
        SOAPElement expiresElement = timestampElement.addChildElement("Expires", "wsu");
        expiresElement
            .setTextContent(createdTimestamp.plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

        SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");
        SOAPElement usernameElement = usernameTokenElement.addChildElement("Username", "wsse");
        usernameElement.setTextContent(username);
        SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
        passwordElement.addAttribute(NameImpl.createFromUnqualifiedName("Type"),
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
        passwordElement.setTextContent(password);
      } catch (SOAPException e) {
        logErrorToSentry(e, applicationFile, routingDestination, applicationNumber, applicationDocument,
            flowType);
      }
    });

    applicationRepository.updateStatus(applicationNumber, applicationDocument, DELIVERED);
  }
  
  @Recover
  public void logErrorToSentry(Exception e, ApplicationFile applicationFile, RoutingDestination routingDestination,
      String applicationNumber, Document applicationDocument, FlowType flowType) {
    applicationRepository.updateStatus(applicationNumber, applicationDocument, DELIVERY_FAILED);
    log.error("Application failed to send: " + applicationFile.getFileName(), e);
  }

  
  private void setPropertiesOnDocument(ApplicationFile applicationFile, RoutingDestination routingDestination, CreateDocument createDocument) {
    CmisPropertiesType properties = new CmisPropertiesType();
    List<CmisProperty> propertiesList = properties.getProperty();
    
    CmisPropertyBoolean read = createCmisPropertyBoolean("Read", false);
    CmisPropertyString originalFileName = createCmisPropertyString("OriginalFileName", applicationFile.getFileName());
    CmisPropertyString cmisName = createCmisPropertyString("cmis:name", applicationFile.getFileName());
    CmisPropertyString fileType = createCmisPropertyString("FileType", "Misc");
    CmisPropertyString npi = createCmisPropertyString("NPI", routingDestination.getDhsProviderId());
    CmisPropertyString mnitsMailboxTransactionType = createCmisPropertyString("MNITSMailboxTransactionType", "OLA");
    CmisPropertyString source = createCmisPropertyString("Source", "MNITS");
    CmisPropertyString flow = createCmisPropertyString("Flow", "Inbound");
    CmisPropertyId cmisObjectTypeId = createCmisPropertyId("cmis:objectTypeId", "MNITSMailbox");
    
    propertiesList
        .addAll(List.of(read, originalFileName, cmisName, fileType, npi, mnitsMailboxTransactionType, source, flow, cmisObjectTypeId));
    createDocument.setProperties(properties);
  }

  @NotNull
  private CmisPropertyString createCmisPropertyString(String propertyDefinitionId, String propertyValue) {
    CmisPropertyString stringProperty = new CmisPropertyString();
    stringProperty.setPropertyDefinitionId(propertyDefinitionId);
    stringProperty.getValue().add(propertyValue);
    return stringProperty;
  }

  @NotNull
  private CmisPropertyBoolean createCmisPropertyBoolean(String propertyDefinitionId, Boolean propertyValue) {
    CmisPropertyBoolean booleanProperty = new CmisPropertyBoolean();
    booleanProperty.setPropertyDefinitionId(propertyDefinitionId);
    booleanProperty.getValue().add(propertyValue);
    return booleanProperty;
  }

  @NotNull
  private CmisPropertyId createCmisPropertyId(String propertyDefinitionId, String propertyValue) {
    CmisPropertyId idProperty = new CmisPropertyId();
    idProperty.setPropertyDefinitionId(propertyDefinitionId);
    idProperty.getValue().add(propertyValue);
    return idProperty;
  }
  
  private void setContentStreamOnDocument(ApplicationFile applicationFile, CreateDocument createDocument) {
    CmisContentStreamType contentStream = new CmisContentStreamType();
    contentStream.setLength(BigInteger.ZERO);
    contentStream.setStream(new DataHandler(new ByteArrayDataSource(applicationFile.getFileBytes(), MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)));
    QName contentStreamQName = new QName("http://docs.oasis-open.org/cmis/CMIS/v1.1/errata01/os/schema/CMIS-Messaging.xsd", "cmisContentStreamType", "cmism");
    JAXBElement<CmisContentStreamType> jaxbContentStream = new JAXBElement<CmisContentStreamType>(contentStreamQName, CmisContentStreamType.class, contentStream);
    createDocument.setContentStream(jaxbContentStream);
  }
  
}
