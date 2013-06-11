package networking;

import generated.MazeCom;
import generated.MazeComType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class XmlInStream extends UTFInputStream {

	private Unmarshaller unmarshaller;

	public XmlInStream(InputStream in) {
		super(in);
		try {
			JAXBContext jc = JAXBContext.newInstance(MazeCom.class);
			this.unmarshaller = jc.createUnmarshaller();
		} catch (JAXBException e) {
			System.err
					.println("[ERROR]: Fehler beim initialisieren der JAXB-Komponenten");
		}
	}

	/**
	 * Liest eine Nachricht und gibt die entsprechende Instanz zurück
	 * 
	 * @return
	 */
	public MazeCom readMazeCom() {
		byte[] bytes = null;
		MazeCom result = null;
		try {
			String xml = this.readUTF8();
			// TODO entfernen
			System.out.println("Empfangen");
			
			bytes = xml.getBytes();
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

			result = (MazeCom) this.unmarshaller.unmarshal(bais);
			if(!result.getMcType().equals(MazeComType.AWAITMOVE))
				System.out.println(xml);
			// TODO entferne Aufgabe
		} catch (JAXBException e) {
			e.printStackTrace();
			System.err
					.println("[ERROR]: Fehler beim unmarshallen der Nachricht");
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("[ERROR]: Fehler beim lesen der Nachricht");
		} catch (NullPointerException e) {
			System.err
					.println("[ERROR]: Nullpointer beim lesen der Nachricht aufgrund weiterer Fehler");
		}
		return result;
	}

}
