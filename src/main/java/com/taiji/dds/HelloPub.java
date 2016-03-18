package com.taiji.dds;


import com.toc.coredx.DDS.*;

public class HelloPub {

  public static void main(String[] args) {
    System.out.println("STARTING -------------------------");
    DomainParticipantFactory dpf = DomainParticipantFactory.get_instance();
    DomainParticipant dp = null;

    System.out.println("CREATE PARTICIPANT ---------------");
    dp = dpf.create_participant(0,    /* domain Id   */
				null, /* default qos */
				null, /* no listener */
				0);

    System.out.println("REGISTERING TYPE -----------------");
    StringMsgTypeSupport ts = new StringMsgTypeSupport();
    ReturnCode_t retval = ts.register_type(dp, null); //"StringMsg");
      
    System.out.println("CREATE TOPIC ---------------------");
    Topic              top          = dp.create_topic("helloTopic", ts.get_type_name(), 
						      null,     // default qos
						      null, 0); // no listener
      
    System.out.println("CREATE PUBLISHER -----------------");
    PublisherQos       pub_qos      = null;
    PublisherListener  pub_listener = null;
    Publisher          pub          = dp.create_publisher(pub_qos, pub_listener, 0);  

    System.out.println("CREATE DATAWRITER ----------------");
    DataWriterQos dw_qos = new DataWriterQos();
    pub.get_default_datawriter_qos(dw_qos);
    dw_qos.entity_name.value = "JAVA_DW";
    DataWriterListener dw_listener = null;
    StringMsgDataWriter   dw = (StringMsgDataWriter) pub.create_datawriter(top, 
								     dw_qos, 
								     dw_listener, 
								     0);

    int i = 1;

    while ( true ) {
      StringMsg data = new StringMsg();
      data.msg    = new String("Hello WORLD from Java!");
      
      System.out.println("WRITE SAMPLE. ");

      /* DDS_HANDLE_NIL (or null) says datawriter should compute handle */
      retval = dw.write ( data, null ); 
      
      if ( retval != ReturnCode_t.RETCODE_OK )
	System.out.println( "   ====  DDS_DataWriter_write() error... ");

      try {
	Thread.currentThread().sleep(1000);   // 1 second sleep
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
  }
};
