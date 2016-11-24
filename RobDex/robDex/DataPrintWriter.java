package robDex;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class DataPrintWriter extends PrintWriter{
	
	OutputStream out;

	public DataPrintWriter(OutputStream out) {
		super(out);
		this.out = out;
	}

	public void write(byte[] b, int off, int len) throws IOException{
		out.write(b, off, len);
	}
}
