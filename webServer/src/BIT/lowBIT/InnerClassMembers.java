package BIT.lowBIT;
import java.io.*;

public class InnerClassMembers {
  public short inner_class_info_index;      	//CONSTANT_Class index
  public short outer_class_info_index;      	//CONSTANT_Class index
  public short inner_name_index;      		//Utf8
  public short inner_class_access_flags;      
  
  // constructor
  public InnerClassMembers(DataInputStream iStream, Cp_Info[] cp) throws IOException {
    inner_class_info_index  = (short) iStream.readUnsignedShort();
    outer_class_info_index  = (short) iStream.readUnsignedShort();
    inner_name_index  = (short) iStream.readUnsignedShort();
    inner_class_access_flags  = (short) iStream.readUnsignedShort();
  }

  public void write(DataOutputStream oStream) throws IOException
  {
    oStream.writeShort((int) inner_class_info_index);
    oStream.writeShort((int) outer_class_info_index);
    oStream.writeShort((int) inner_name_index);
    oStream.writeShort((int) inner_class_access_flags);
  }
}
