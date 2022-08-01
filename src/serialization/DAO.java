package serialization;

import java.io.FileNotFoundException;
import java.io.IOException;

// interface used by all object-specific DAO methods (for save)
public interface DAO {
	
	public void create() throws FileNotFoundException, IOException, Exception;
	
	public void read() throws FileNotFoundException, IOException, Exception;
	
	public void update();
	
	public void delete();
	
}
