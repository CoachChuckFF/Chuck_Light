deps_config := \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/app_trace/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/aws_iot/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/bt/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/esp32/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/ethernet/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/fatfs/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/freertos/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/heap/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/libsodium/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/log/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/lwip/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/mbedtls/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/openssl/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/pthread/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/spi_flash/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/spiffs/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/tcpip_adapter/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/wear_levelling/Kconfig \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/bootloader/Kconfig.projbuild \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/esptool_py/Kconfig.projbuild \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/components/partition_table/Kconfig.projbuild \
	/home/penguin/Documents/Blizzard/compiling_files/esp-idf/Kconfig

include/config/auto.conf: \
	$(deps_config)


$(deps_config): ;
