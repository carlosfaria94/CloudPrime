package BIT.lowBIT;
import java.io.*;

public class InnerClasses_Attribute extends Attribute_Info {
  public short number_of_classes;
  public InnerClassMembers classes[];

  // constructor
  public InnerClasses_Attribute(DataInputStream iStream, short attribute_name_index, Cp_Info[] cp) throws IOException {
    this.attribute_name_index = attribute_name_index;
    attribute_length = iStream.readInt();
    if (System.getProperty("CJKDEBUG3") != null) 
    System.err.println("READ BIT parsing len: " + attribute_length);
    number_of_classes  = (short) iStream.readUnsignedShort();
    classes = new InnerClassMembers[number_of_classes];
    for (int i = 0; i < number_of_classes; i++) {
	classes[i] = new InnerClassMembers(iStream,cp);
    }
  }

  public void write(DataOutputStream oStream) throws IOException
  {
    oStream.writeShort((int) attribute_name_index);
    oStream.writeInt(attribute_length);
    oStream.writeShort((int) number_of_classes);
    for (int i = 0; i < number_of_classes; i++) {
	classes[i].write(oStream);
    }
  }
}
