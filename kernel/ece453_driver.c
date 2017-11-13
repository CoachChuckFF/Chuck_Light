/*
 * Sample kobject implementation
 *
 * Copyright (C) 2004-2007 Greg Kroah-Hartman <greg@kroah.com>
 * Copyright (C) 2007 Novell Inc.
 *
 * Released under the GPL version 2 only.
 *
 */
#include <linux/kobject.h>
#include <linux/string.h>
#include <linux/sysfs.h>
#include <linux/module.h>
#include <linux/init.h>
#include <linux/kernel.h>
#include <linux/init.h>
#include <linux/fs.h>
#include <linux/device.h>
#include <linux/types.h>
#include <linux/mutex.h>
#include <linux/platform_device.h>  /* Needed for Platform Driver Functions */ 
#include <linux/irq.h>
#include <linux/interrupt.h>
#include <linux/signal.h>
#include <asm/siginfo.h>
#include <linux/module.h>
#include <linux/sched.h>    //find_task_by_pid_type
#include <linux/debugfs.h>
#include <linux/uaccess.h>

#include "ece453.h"

#define SIG_TEST 44


unsigned long *base_addr;   /* Vitual Base Address */
struct resource *res;       /* Device Resource Structure */
unsigned long remap_size;   /* Device Memory Size */

static int irq;
static int pid;

/*
 * This module shows how to create a simple subdirectory in sysfs called
 * /sys/kernel/kobject-example  In that directory, 3 files are created:
 * "foo", "read", and "write".  If an integer is written to these files, it can be
 * later read out of it.
 */

/* Module parameters that can be provided on insmod */
static bool debug = true;   /* print extra debug info */
module_param(debug, bool, S_IRUGO | S_IWUSR);
MODULE_PARM_DESC(debug, "enable debug info (default: false)");

/*****************************************************************************/
/* Handles any interrupts that are generated by the IP module                */
/*****************************************************************************/
static irqreturn_t ece453_irq_handler(int irq, void *dev_id)
{
  struct siginfo info;
  struct task_struct *t;
  int ret;
  int active_irqs = 0;
  
  // Read in the currently active interrupts 
  active_irqs = ioread32(base_addr + ECE453_IRQ_OFFSET);

  // Acknowledge all the active interrupts
  iowrite32( active_irqs , base_addr + ECE453_IRQ_OFFSET);

  /* prepare the signal */
  memset(&info, 0, sizeof(struct siginfo));
  info.si_signo = SIG_TEST;
  info.si_code = SI_QUEUE;
    
  // Send the list of active IRQs back to the user. 
  info.si_int = active_irqs;

  t = pid_task(find_pid_ns(pid, &init_pid_ns), PIDTYPE_PID);    
  if(t == NULL){
      pr_err("no such pid\n");
  }
  else
  {
    ret = send_sig_info(SIG_TEST, &info, t);    //send the signal
    if (ret < 0) {
      pr_err("error sending signal\n");
    }
  }
  
  return IRQ_HANDLED;
}

/*****************************************************************************/
/* Read Functions                                                            */
/*****************************************************************************/

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_device_id ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_DEV_ID_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_control ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_CONTROL_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_status ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_STATUS_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_im ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_IM_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_irq ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_IRQ_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_gpio_in ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_GPIO_IN_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_gpio_out ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_GPIO_OUT_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_dmx_addr ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_DMX_ADDR_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_dmx_data ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_DMX_DATA_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_read_dmx_size ( 
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    char *buf
    )
{
  int val = ioread32(base_addr + ECE453_DMX_SIZE_OFFSET);
  return sprintf(buf, "%08x\n", val);
}

/*****************************************************************************/
/* Write Functions                                                           */
/*****************************************************************************/

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_control (
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    const char *buf, 
    size_t count
    )
{
  int var;
  sscanf(buf, "%xu", &var);
  iowrite32( var, base_addr + ECE453_CONTROL_OFFSET);
  return count;
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_im (
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    const char *buf, 
    size_t count
    )
{
  int var;
  sscanf(buf, "%xu", &var);
  iowrite32( var, base_addr + ECE453_IM_OFFSET);
  return count;
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_irq (
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    const char *buf, 
    size_t count
    )
{
  int var;
  sscanf(buf, "%xu", &var);
  iowrite32( var, base_addr + ECE453_IRQ_OFFSET);
  return count;
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_gpio_out (
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    const char *buf, 
    size_t count
    )
{
  int var;
  sscanf(buf, "%xu", &var);
  iowrite32( var, base_addr + ECE453_GPIO_OUT_OFFSET);
  return count;
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_dmx_addr (
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    const char *buf, 
    size_t count
    )
{
  int var;
  sscanf(buf, "%xu", &var);
  iowrite32( var, base_addr + ECE453_DMX_ADDR_OFFSET);
  return count;
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_dmx_data (
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    const char *buf, 
    size_t count
    )
{
  int var;
  sscanf(buf, "%xu", &var);
  iowrite32( var, base_addr + ECE453_DMX_DATA_OFFSET);
  return count;
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_dmx_size (
    struct kobject *kobj, 
    struct kobj_attribute *attr, 
    const char *buf, 
    size_t count
    )
{
  int var;
  sscanf(buf, "%xu", &var);
  iowrite32( var, base_addr + ECE453_DMX_SIZE_OFFSET);
  return count;
}

/*****************************************************************************/
/*****************************************************************************/
static ssize_t ece453_write_pid(struct kobject *kobj, struct kobj_attribute *attr,
                       const char *buf, size_t count)
{
    sscanf(buf, "%d", &pid);

  dbg("pid %d\n",pid);

  return count;
}
    



// The control and status register is read write, so we set the file permissions
// to be read/write (0666) and set up the read and write functions 
static struct kobj_attribute device_id_attribute =
        __ATTR(device_id, 0444, ece453_read_device_id, NULL);

static struct kobj_attribute control_attribute =
        __ATTR(control, 0664, ece453_read_control, ece453_write_control);

static struct kobj_attribute status_attribute =
        __ATTR(status, 0444, ece453_read_status, NULL);

static struct kobj_attribute im_attribute =
        __ATTR(interrupt_mask, 0664, ece453_read_im, ece453_write_im);

static struct kobj_attribute irq_attribute =
        __ATTR(irq, 0664, ece453_read_irq, ece453_write_irq);

static struct kobj_attribute gpio_in_attribute =
        __ATTR(gpio_in, 0444, ece453_read_gpio_in, NULL);

static struct kobj_attribute gpio_out_attribute =
        __ATTR(gpio_out, 0664, ece453_read_gpio_out, ece453_write_gpio_out);

static struct kobj_attribute dmx_addr_attribute =
        __ATTR(dmx_addr, 0664, ece453_read_dmx_addr, ece453_write_dmx_addr);
static struct kobj_attribute dmx_data_attribute =
        __ATTR(dmx_data, 0664, ece453_read_dmx_data, ece453_write_dmx_data);
static struct kobj_attribute dmx_size_attribute =
        __ATTR(dmx_size, 0664, ece453_read_dmx_size, ece453_write_dmx_size);

static struct kobj_attribute pid_attribute =
        __ATTR(pid, 0220, NULL, ece453_write_pid);

/*
 * Create a group of attributes so that we can create and destory them all
 * at once.
 */
static struct attribute *attrs[] = {
        &device_id_attribute.attr,
        &control_attribute.attr,
        &status_attribute.attr,
        &im_attribute.attr,
        &irq_attribute.attr,
        &gpio_in_attribute.attr,
        &gpio_out_attribute.attr,
        &dmx_addr_attribute.attr,
        &dmx_data_attribute.attr,
        &dmx_size_attribute.attr,
        &pid_attribute.attr,
        NULL   /* need to NULL terminate the list of attributes */
};

/*
 * An unnamed attribute group will put all of the attributes directly in
 * the kobject directory.  If we specify a name, a subdirectory will be
 * created for the attributes with the directory being the name of the
 * attribute group.
 */
static struct attribute_group attr_group = {
        .attrs = attrs,
};

static struct kobject *ece453_obj;

/*
 *
 */
static void ece453_shutdown(struct platform_device *pdev)
{
    /* Clear any interrupts */
    iowrite32( 0xFFFFFFFF, base_addr + ECE453_IRQ_OFFSET);


    /* Turn Interrupts Off */
    iowrite32( 0, base_addr + ECE453_IM_OFFSET);
}

/* Remove function for mytimer
 * When mytimer module is removed, turn off all the leds first,
 * release virtual address and the memory region requested.
 */
static int ece453_remove(struct platform_device *pdev)
{
  ece453_shutdown(pdev);

    iounmap(base_addr);
    
    /* Release the region */
    release_mem_region(res->start, remap_size);

  kobject_put(ece453_obj);

  free_irq(irq,pdev);

    return 0;
}


/**
 * ece453_drv_probe -  Probe call for the device.
 *
 * @pdev:   handle to the platform device structure.
 *
 **/
static int ece453_probe(struct platform_device *pdev)
{
    int ret = 0;

    res = platform_get_resource(pdev, IORESOURCE_MEM, 0);
    if (!res) {
        dev_err(&pdev->dev, "No memory resource\n");
        return -ENODEV;
    }

    remap_size = res->end - res->start + 1;
    dev_info(&pdev->dev, "remap_size = %d\n", (int)remap_size);
    if (!request_mem_region(res->start, remap_size, pdev->name)) {
        dev_err(&pdev->dev, "Cannot request IO\n");
        return -ENXIO;
    }

    base_addr = ioremap(res->start, remap_size);
    if (base_addr == NULL) {
        dev_err(&pdev->dev, "Couldn't ioremap memory at 0x%08lx\n",
                (unsigned long)res->start);
        
        release_mem_region(res->start, remap_size);

        return -ENOMEM;
    }
 
  /* Register the interrupt */
    irq = platform_get_irq(pdev, 0);
    if (irq >= 0) {
        ret = request_irq(irq, ece453_irq_handler, 0, pdev->name, pdev);
        if (ret != 0) {
            goto err_irq;
        }
    }


 /*
  * Create a simple kobject with the name of "ece453_example",
  * located under /sys/kernel/
  *
  * As this is a simple directory, no uevent will be sent to
  * userspace.  That is why this function should not be used for
  * any type of dynamic kobjects, where the name and number are
  * not known ahead of time.
  */
 ece453_obj = kobject_create_and_add("ece453", kernel_kobj);
 if (!ece453_obj)
         return -ENOMEM;

 /* Create the files associated with this kobject */
 ret = sysfs_create_group(ece453_obj, &attr_group);
 if (ret)
    {
                kobject_put(ece453_obj);
                return -ENOMEM;
    }

    return 0;

err_irq:
    free_irq(irq, pdev);
err_release_region:
    release_mem_region(res->start, remap_size);
 return -1;

}

/* device match table to match with device node in device tree */
static const struct of_device_id ece453_of_match[] = {
    {.compatible = "uw_madison,ece453-1.0"},
    {},
};

/* platform driver structure for mytimer driver */
static struct platform_driver ece453_platform_driver = {
    .driver = {
        .name = DRIVER_NAME,
        .owner = THIS_MODULE,
        .of_match_table = ece453_of_match},
    .probe = ece453_probe,
    .remove = ece453_remove,
    .shutdown = ece453_shutdown
};



/**
 * ece453_module_init -  register the Device Configuration.
 *
 * Returns 0 on success, otherwise negative error.
 */
static int __init example_init(void)
{
    return platform_driver_register(&ece453_platform_driver);
}

/**
 * ece453_module_exit -  Unregister the Device Configuration.
 */
static void __exit example_exit(void)
{
    platform_driver_unregister(&ece453_platform_driver);

}

module_init(example_init);
module_exit(example_exit);
MODULE_LICENSE("GPL");
MODULE_AUTHOR(" <Joe Krachey, jkrachey@wisc.edu>");

