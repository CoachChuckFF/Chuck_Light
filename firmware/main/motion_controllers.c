/*motion_controllers.c*/
#include "lib/motion_controllers.h"

#define TAG "Motion Controller"

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
  write_motion_reg(0x0A, 0x56);
  ESP_LOGI(TAG, "%02X", read_motion_reg(0x0A));
  ESP_LOGI(TAG, "%02X", read_motion_reg(0x0F));
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

void read_motion(uint8_t *ret_val)
{
  esp_err_t ret;
  spi_transaction_t t;

  memset(tx_buffer, 0, 6);
  memset(rx_buffer, 0, 6);
  memset(&t, 0, sizeof(t));       //Zero out the transaction

  t.length=48;                     //Command is 8 bits
  t.rxlength=48;
  t.tx_buffer=NULL;               //No data
  t.rx_buffer=rx_buffer;
  t.cmd=0x80 | 0x28;

/*
  ret=spi_device_transmit(spi, &t);  //Transmit!
  assert(ret==ESP_OK);            //Should have had no issues.
  ret_val[0] = rx_buffer[0];
  ret_val[1] = rx_buffer[1];
  ret_val[2] = rx_buffer[2];
  ret_val[3] = rx_buffer[3];
  ret_val[4] = rx_buffer[4];
  ret_val[5] = rx_buffer[5];*/


  ret_val[0] = read_motion_reg(0x22);
  ret_val[1] = read_motion_reg(0x23);
  ret_val[2] = read_motion_reg(0x24);
  ret_val[3] = read_motion_reg(0x25);
  ret_val[4] = read_motion_reg(0x26);
  ret_val[5] = read_motion_reg(0x27);

  ESP_LOGI(TAG, "\n X: %02X, %02X\n Y: %02X, %02X\n Z: %02X, %02X\n",
                                                                ret_val[0],
                                                                ret_val[1],
                                                                ret_val[2],
                                                                ret_val[3],
                                                                ret_val[4],
                                                                ret_val[5]);


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

  t.length = 16;                  //Command is 8 bits + 8 tx bits
  t.tx_buffer = tx_buffer;        //write buffer
  //t.rx_buffer=NULL;             //No read phase
  t.cmd = WRITE | reg;

  //data to write to t.cmd's register
  tx_buffer[0] = val;

  ret=spi_device_transmit(spi, &t);  //Transmit!
  assert(ret==ESP_OK);            //Should have had no issues.
}
