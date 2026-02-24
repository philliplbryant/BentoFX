/*******************************************************************************
 This is an unpublished work of SAIC.
 Copyright (c) 2026 SAIC. All Rights Reserved.
 ******************************************************************************/

package software.coley.bentofx.persistence.impl.codec.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import software.coley.bentofx.persistence.api.codec.BentoState;
import software.coley.bentofx.persistence.api.codec.BentoStateException;
import software.coley.bentofx.persistence.api.codec.LayoutCodec;
import software.coley.bentofx.persistence.impl.codec.common.mapper.BentoStateMapper;
import software.coley.bentofx.persistence.impl.codec.common.mapper.dto.BentoStateListDto;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;

/**
 * XML codec for {@link BentoState} using Jakarta JAXB.
 *
 * @author Phil Bryant
 */
public final class XmlLayoutCodec implements LayoutCodec {

    private final JAXBContext context;

    public XmlLayoutCodec() {
        try {
            this.context = JAXBContext.newInstance(BentoStateListDto.class);
        } catch (final JAXBException e) {
            throw new IllegalStateException("Failed to initialize JAXBContext", e);
        }
    }

    @Override
    public String getIdentifier() {
        return "xml";
    }

    @Override
    public void encode(
            final @NotNull List<@NotNull BentoState> bentoStateList,
            final @NotNull OutputStream outputStream
    ) throws BentoStateException {

        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            final BentoStateListDto dto = BentoStateMapper.toDto(bentoStateList);

            // Marshal to DOM for better "pretty printing"
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
            marshaller.marshal(dto, document);

            // Pretty print DOM to output stream
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");

            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    "2"
            );
            transformer.transform(
                    new DOMSource(document),
                    new StreamResult(outputStream)
            );
        } catch (final Exception e) {

            throw new BentoStateException("Failed to encode BentoState as XML", e);
        }
    }

    @Override
    public @NotNull List<@NotNull BentoState> decode(final @NotNull InputStream inputStream) throws BentoStateException {
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final Object obj = unmarshaller.unmarshal(inputStream);
            if (!(obj instanceof final BentoStateListDto dto)) {
                throw new BentoStateException("Unexpected JAXB root type: " + obj);
            }
            return BentoStateMapper.fromDto(dto);
        } catch (final JAXBException e) {
            throw new BentoStateException("Failed to unmarshall BentoState from XML", e);
        }
    }
}
