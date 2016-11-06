package robDex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class DataBufferedReader extends BufferedReader{
	
	public DataBufferedReader(Reader in) {
		super(in);
	}

	public DataBufferedReader(InputStream in){
		this(new InputStreamReader(in));
	}
	
	public int readInt() throws ArithmeticException, NumberFormatException, IOException{
		
		long l = readLong();
		
		int i = (int) l;
		
		if((long)i != l)
			throw new ArithmeticException();
		
		return i;
	}
	
	public long readLong() throws NumberFormatException, IOException{
		
		return Long.parseLong(readLine());
	}
}
