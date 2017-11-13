import java.nio.file.*;
import java.io.IOException;
import java.util.Arrays;

public class DMXDriver {

  private static final String ADDR_FILENAME = "/sys/kernel/ece453/dmx_addr";
  private static final String DATA_FILENAME = "/sys/kernel/ece453/dmx_data";
  private static final String SIZE_FILENAME = "/sys/kernel/ece453/dmx_size";
  private static final String CTRL_FILENAME = "/sys/kernel/ece453/control";
  private static final String STAT_FILENAME = "/sys/kernel/ece453/status";

  private Path addr;
  private Path data;
  private Path size;
  private Path ctrl;
  private Path stat;

  public DMXDriver() throws IOException {
    addr = Paths.get(ADDR_FILENAME);
    data = Paths.get(DATA_FILENAME);
    size = Paths.get(SIZE_FILENAME);
    ctrl = Paths.get(CTRL_FILENAME);
    stat = Paths.get(STAT_FILENAME);

    if (!Files.isWritable(addr))
      throw new IOException(ADDR_FILENAME + " not writable.");
    if (!Files.isWritable(data))
      throw new IOException(DATA_FILENAME + " not writable.");
    if (!Files.isWritable(size))
      throw new IOException(SIZE_FILENAME + " not writable.");
    if (!Files.isWritable(ctrl))
      throw new IOException(CTRL_FILENAME + " not writable.");
    if (!Files.isReadable(stat))
      throw new IOException(STAT_FILENAME + " not readable.");
  }

  public void setDMX(int address, int... values) throws IOException {
    if (address <= 0 || address > 512)
      throw new IllegalArgumentException("DMX address must be within [1:512]");
    if (values.length == 0)
      throw new IllegalArgumentException("Must supply at least one value.");
    if (values.length > 4)
      throw new IllegalArgumentException("Must supply at most four values.");
    if (Arrays.stream(values).anyMatch(val -> val < 0 || val > 255))
      throw new IllegalArgumentException("DMX values must be within [0:255]");

    int data_val = values[0];
    if (values.length > 1)
      data_val = (values[1] & 0xff) << 8;
    if (values.length > 2)
      data_val = (values[2] & 0xff) << 16;
    if (values.length > 3)
      data_val = (values[3] & 0xff) << 14;

    write_reg(data, data_val);
    write_reg(addr, address);
    write_reg(size, values.length);
    write_reg(ctrl, 0x1);
  }

  public void clearDMX() throws IOException {
    for (int i = 1 ; i < 512 ; i = i + 4) {
      setDMX(i, 0, 0, 0, 0);
    }
  }

  private static void write_reg(Path reg, int val) throws IOException {
    byte[] buf = String.format("%08x\0", val).getBytes();
    if (buf.length != 9)
      throw new IllegalArgumentException(String.format("buffer length was %d from value %08x when trying to write to %s", buf.length, val, reg.toString()));

    Files.write(reg, buf, StandardOpenOption.SYNC);
  }
}
