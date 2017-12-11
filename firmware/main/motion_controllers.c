/*motion_controllers.c*/
#include "lib/motion_controllers.h"

#define TAG "Motion Controller"

#define AVE_SAMPLE 13

spi_device_handle_t spi;
char* tx_buffer;
char* rx_buffer;

void init_motion_controllers()
{
  esp_err_t ret;
  spi_bus_config_t buscfg = {
      .miso_io_num = MISO_PIN,
      .mosi_io_num = MOSI_PIN,
      .sclk_io_num = CLK_PIN,
      .quadwp_io_num = -1,
      .quadhd_io_num = -1
  };
  spi_device_interface_config_t devcfg={
      .clock_speed_hz = 9*1000*1000,        //Clock out at 9 MHz
      .mode = 0,                            //SPI mode 0
      .spics_io_num = CS_PIN,               //CS pin
      .queue_size = 1,                      //1 queue size
      .command_bits = 8,                    //command = R/W + Register Address
      .address_bits = 0,                    //no address phase
      .dummy_bits = 0                       //no dummy phase
      /*
      .pre_cb
      .post_cb
      */
  };
  //Initialize the SPI bus
  ret=spi_bus_initialize(HSPI_HOST, &buscfg, 1);
  assert(ret==ESP_OK);
  //Attach the LCD to the SPI bus
  ret=spi_bus_add_device(HSPI_HOST, &devcfg, &spi);
  assert(ret==ESP_OK);

  //Alloc Buffers
  tx_buffer = heap_caps_malloc(32, MALLOC_CAP_DMA);
  rx_buffer = heap_caps_malloc(32, MALLOC_CAP_DMA);

  //restart gyroscope + accelerometer
  write_motion_reg(0x10, 0xA0);
  write_motion_reg(0x17, 0b01100100);
  write_motion_reg(0x58, 0b00010000);

}

void restart_motion_device()
{
  esp_err_t ret;
  spi_transaction_t t;

  memset(&t, 0, sizeof(t));       //Zero out the transaction

  t.length = 16;                  //Command is 8 bits + 8 tx bits
  t.tx_buffer = tx_buffer;        //write buffer
  //t.rx_buffer=NULL;             //No read phase
  t.cmd = WRITE | CTRL_REG8;

  //data to write to t.cmd's register
  tx_buffer[0] = REBOOT_MOTION;

  ret=spi_device_transmit(spi, &t);  //Transmit!
  assert(ret==ESP_OK);            //Should have had no issues.
}

int read_motion()
{
  int x;
  int y;
  int z;

  x = (int) ((int16_t)(((int16_t)read_motion_reg(0x28)) | (((int16_t)read_motion_reg(0x29)) << 8)));
  y = (int) ((int16_t)(((int16_t)read_motion_reg(0x2A)) | (((int16_t)read_motion_reg(0x2B)) << 8)));
  z = (int) ((int16_t)(((int16_t)read_motion_reg(0x2C)) | (((int16_t)read_motion_reg(0x2D)) << 8)));

  return get_movement_magnitude(x, y, z);

}

int get_movement_magnitude(int x, int y, int z)
{
  static int x_ave[AVE_SAMPLE];
  static int y_ave[AVE_SAMPLE];
  static int z_ave[AVE_SAMPLE];
  static int count = -1;

  int movement_magnitude = 0;
  int x_mag = get_average(x_ave) - x;
  int y_mag = get_average(y_ave) - y;
  int z_mag = get_average(z_ave) - z;

  movement_magnitude += (x_mag < 0) ? -1 * x_mag : x_mag;
  movement_magnitude += (y_mag < 0) ? -1 * y_mag : y_mag;
  movement_magnitude += (z_mag < 0) ? -1 * z_mag : z_mag;

  if(++count >= AVE_SAMPLE)
  {
    count = 0;
  }

  x_ave[count] = x;
  y_ave[count] = y;
  z_ave[count] = z;

  return movement_magnitude;

}

int get_average(int *buf)
{
  uint8_t i = 0;
  int ave = 0;

  for(i = 0; i < AVE_SAMPLE; i++)
  {
    ave += buf[i];
  }

  return ave/AVE_SAMPLE;
}

uint8_t read_motion_reg(uint8_t reg)
{
  esp_err_t ret;
  spi_transaction_t t;

  memset(rx_buffer, 0, 1);
  memset(&t, 0, sizeof(t));       //Zero out the transaction

  t.length=8;                     //Command is 8 bits
  t.rxlength=8;
  t.tx_buffer=NULL;               //No data
  t.rx_buffer=rx_buffer;
  t.cmd=0x80 | reg;

  ret=spi_device_transmit(spi, &t);  //Transmit!
  assert(ret==ESP_OK);              //Should have had no issues.

  return rx_buffer[0];
}


void write_motion_reg(uint8_t reg, uint8_t val)
{
  esp_err_t ret;
  spi_transaction_t t;

  memset(&t, 0, sizeof(t));       //Zero out the transaction

  t.length = 8;                  //Command is 8 bits + 8 tx bits
  t.tx_buffer = tx_buffer;        //write buffer
  //t.rx_buffer=NULL;             //No read phase
  t.cmd = WRITE | reg;

  //data to write to t.cmd's register
  tx_buffer[0] = val;

  ret=spi_device_transmit(spi, &t);  //Transmit!
  assert(ret==ESP_OK);            //Should have had no issues.
}
