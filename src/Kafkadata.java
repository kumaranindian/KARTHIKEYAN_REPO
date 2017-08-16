import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class Kafkadata {

	public Kafkadata(String url) {
		putData(url);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		putData("Sample Method");
	}

	public static void putData(String stringmessage) {
		// TODO Auto-generated method stub
		try {
			
			System.out.println("Start of putData method ===>"+stringmessage);
			String key="key1";
			String value="Value1";
			Properties props=new Properties();
			props.put("zk.connect","localhost:2181");
			props.put("serializer.class","kafka.serializer.StringEncoder");
			props.put("metadata.broker.list","localhost:9092");
			ProducerConfig config=new ProducerConfig(props);
			Producer  producer=new Producer(config);
		//	String msg="THIS IS KARTHI'S FIRST DEMO";
			String msg=stringmessage;
			producer.send(new KeyedMessage("demo",msg));
			producer.close();
			System.out.println("END");
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			// TODO: handle exception
		}
	}

}
