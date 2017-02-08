package BIT.lowBIT;
import java.io.*;

public class Deprecated_Attribute extends Attribute_Info {
  
  //classfile, field_info, or method_info attribute

  // constructor
  public Deprecated_Attribute(DataInputStream iStream, short attribute_name_index) throws IOException {
    this.attribute_name_index = attribute_name_index;
    attribute_length = iStream.readInt();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing len: " + attribute_length);
  }

  public void write(DataOutputStream oStream) throws IOException
  {
    oStream.writeShort((int) attribute_name_index);
    oStream.writeInt(attribute_length);
  }
}
