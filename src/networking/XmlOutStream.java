package networking;

import generated.MazeCom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class XmlOutStream extends UTFOutputStream {

	private Marshaller marshaller;

	public XmlOutStream(OutputStream out) {
		super(out);
		// Anlegen der JAXB-Komponenten
		try {
			JAXBContext jc = JAXBContext.newInstance(MazeCom.class);
			this.marshaller = jc.createMarshaller();
			this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			this.marshaller = jc.createMarshaller();
		} catch (JAXBException e) {
			System.err.println("[ERROR]: Fehler beim Initialisieren der JAXB-Komponenten");
		}
	}

	/**
	 * Versenden einer XML Nachricht
	 * 
	 * @param mc
	 */
	public void write(MazeCom mc) {
		// gernerierung des fertigen XML
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			this.marshaller.marshal(mc, baos);
			// Versenden des XML
			this.writeUTF8(new String(baos.toByteArray()));
			this.flush();
		} catch (IOException e) {
			System.err.println("[ERROR]: Fehler beim versendern der Nachricht");
		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
