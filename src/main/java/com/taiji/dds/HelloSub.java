package com.taiji.dds;


import com.toc.coredx.DDS.*;

public class HelloSub {

  public static void main(String[] args) {

    class TestDataReaderListener implements DataReaderListener 
    {
      public long get_nil_mask() { return 0; }

      public void on_requested_deadline_missed(DataReader dr,
					       RequestedDeadlineMissedStatus status) { 
	System.out.println(" @@@@@@@@@@@     REQUESTED DEADLINE MISSED    @@@@@"); 
	System.out.println(" @@@@@@@@@@@                                  @@@@@" );
	System.out.println(" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"); 
      };

      public void on_requested_incompatible_qos(DataReader dr,
						RequestedIncompatibleQosStatus status) { 
	System.out.println(" @@@@@@@@@@@     REQUESTED INCOMPAT QOS    @@@@@@@@"); 
	System.out.println(" @@@@@@@@@@@        dr      = " + dr);
	System.out.println(" @@@@@@@@@@@                               @@@@@@@@" );
	System.out.println(" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"); 
      };

      public void on_sample_rejected(DataReader dr, 
				     SampleRejectedStatus status) { 
      };

      public void on_liveliness_changed(DataReader dr,
					LivelinessChangedStatus status)
      {
	TopicDescription   td = dr.get_topicdescription();
	System.out.println(" @@@@@@@@@@@     LIVELINESS CHANGED      @@@@@@@@@@"); 
	System.out.println(" @@@@@@@@@@@        topic   = " + td.get_name() + " (type: " + td.get_type_name() + ")");
	System.out.println(" @@@@@@@@@@@        change  = " + status.get_alive_count_change());
	System.out.println(" @@@@@@@@@@@        current = " + status.get_alive_count());
	System.out.println(" @@@@@@@@@@@                             @@@@@@@@@@" );
	System.out.println(" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"); 
      }

      public void on_subscription_matched(DataReader dr, 
					  SubscriptionMatchedStatus status)
      { 
	TopicDescription   td = dr.get_topicdescription();
	System.out.println(" @@@@@@@@@@@     SUBSCRIPTION MATCHED    @@@@@@@@@@"); 
	System.out.println(" @@@@@@@@@@@        topic   = " + td.get_name() + " (type: " + td.get_type_name() + ")");
	System.out.println(" @@@@@@@@@@@        current = " + status.get_current_count());
	System.out.println(" @@@@@@@@@@@                             @@@@@@@@@@" );
	System.out.println(" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"); 
      }

      public void on_sample_lost(DataReader dr, 
				 SampleLostStatus status) { 
      };


      public void on_data_available(DataReader dr)
      { 
	TopicDescription td = dr.get_topicdescription();

	System.out.println(" @@@@@@@@@@@     DATA AVAILABLE          @@@@@@@@@@"); 
	System.out.println(" @@@@@@@@@@@        topic = " + td.get_name() + " (type: " + td.get_type_name() + ")");

	StringMsgDataReader string_dr = (StringMsgDataReader)dr;
	StringMsgSeq     samples = new StringMsgSeq();
	SampleInfoSeq si      = new SampleInfoSeq();
	ReturnCode_t  retval  = string_dr.take(samples, si, 100, 
					       coredx.DDS_ANY_SAMPLE_STATE, 
					       coredx.DDS_ANY_VIEW_STATE, 
					       coredx.DDS_ANY_INSTANCE_STATE);
	System.out.println(" @@@@@@@@@@@        DR.read() ===> " + retval);

	if (retval == ReturnCode_t.RETCODE_OK)
	  {
	    if (samples.value == null)
	      System.out.println(" @@@@@@@@@@@        samples.value = null");
	    else
	      {
		System.out.println(" @@@@@@@@@@@        samples.value.length= " + samples.value.length);
		for (int i = 0; i < samples.value.length; i++)
		  {
		    System.out.println("    State       : " + 
				       (si.value[i].instance_state == 
					coredx.DDS_ALIVE_INSTANCE_STATE?"ALIVE":"NOT ALIVE") );
		    System.out.println("    TimeStamp   : " + si.value[i].source_timestamp.sec + "." + si.value[i].source_timestamp.nanosec);
		    System.out.println("    Handle      : " + si.value[i].instance_handle.value);
		    System.out.println("    WriterHandle: " + si.value[i].publication_handle.value);
		    System.out.println("    SampleRank  : " + si.value[i].sample_rank);
		    if (si.value[i].valid_data)
		      System.out.println("       msg      : " + samples.value[i].msg);
		  }
	      }
	    string_dr.return_loan(samples, si);
	  }
	else
	  {
	  }
	System.out.println(" @@@@@@@@@@@                             @@@@@@@@@@" );
	System.out.println(" @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"); 
      };
    };




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
						      null, // default qos
						      null, 0); // no listener
      
    System.out.println("CREATE SUBSCRIBER ----------------");
    SubscriberQos       sub_qos      = null;
    SubscriberListener  sub_listener = null;
    Subscriber          sub          = dp.create_subscriber(sub_qos, sub_listener, 0);  

    System.out.println("CREATE DATAREADER ----------------");
    DataReaderQos dr_qos = new DataReaderQos();
    sub.get_default_datareader_qos(dr_qos);
    dr_qos.entity_name.value = "JAVA_DR";
    dr_qos.history.depth = 10;
    DataReaderListener dr_listener = new TestDataReaderListener();
    StringMsgDataReader   dr = (StringMsgDataReader) sub.create_datareader(top, 
								     dr_qos, 
								     dr_listener, 
								     coredx.getDDS_ALL_STATUS());

    
    while ( true ) {
      try {
	Thread.currentThread().sleep(5000);   // 5 second sleep
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
    
  }
};
