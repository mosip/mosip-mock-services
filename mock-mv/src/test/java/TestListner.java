//package io.mosip.mock.mv;

import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.queue.Listener;
import io.mosip.mock.mv.service.ExpectationCache;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

//import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.*;
import javax.jms.Connection;

import javax.jms.*;


@RunWith(SpringRunner.class)
public class TestListner {
    @Mock
    private ExpectationCache expectationCache;

    @Mock
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    @Mock
    private Session session;

    @Mock
    private Environment env;




    @InjectMocks
    Listener listener;
    @Mock
    private Connection connection;

    @Mock
    ActiveMQTextMessage message=new ActiveMQTextMessage();
    @Mock
    ConnectionFactory connectionFactory;

    @Mock
    MessageProducer messageProducer;

    @Mock
    Queue queue;

    @Mock
    BytesMessage bytesMessage;


    String jsonString1;



    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        jsonString1 = "{\n" +
                "  \"id\" : \"mosip.manual.adjudication.adjudicate\",\n" +
                "  \"version\" : \"1.0\",\n" +
                "  \"requestId\" : \"d9a75df6-1b96-4f61-934c-b705c1409e81\",\n" +
                "  \"referenceId\" : \"10002100800001020230223050340\",\n" +
                "  \"requesttime\" : \"2023-07-17T12:27:25.971Z\",\n" +
                "  \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/mpolicy-default-adjudication/mpartner-default-adjudication/mpartner-default-adjudicationmpolicy-default-adjudication20230717122729JCF1Dyjv\",\n" +
                "  \"addtional\" : null,\n" +
                "  \"gallery\" : {\n" +
                "    \"referenceIds\" : [ {\n" +
                "      \"referenceId\" : \"10002100800000920230221130731\",\n" +
                "      \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/mpolicy-default-adjudication/mpartner-default-adjudication/mpartner-default-adjudicationmpolicy-default-adjudication202307171227320voLjt1o\"\n" +
                "    }, {\n" +
                "      \"referenceId\" : \"10007100090003520230222093240\",\n" +
                "      \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/mpolicy-default-adjudication/mpartner-default-adjudication/mpartner-default-adjudicationmpolicy-default-adjudication20230717122734iwSybeFH\"\n" +
                "    }, {\n" +
                "      \"referenceId\" : \"10002100800000820230221115508\",\n" +
                "      \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/mpolicy-default-adjudication/mpartner-default-adjudication/mpartner-default-adjudicationmpolicy-default-adjudication20230717122737OqqWdRCu\"\n" +
                "    }, {\n" +
                "      \"referenceId\" : \"10007100090001520230208093552\",\n" +
                "      \"referenceURL\" : \"http://datashare.datashare/v1/datashare/get/mpolicy-default-adjudication/mpartner-default-adjudication/mpartner-default-adjudicationmpolicy-default-adjudication20230717122739X4ykJPHY\"\n" +
                "    } ]\n" +
                "  }\n" +
                "}";

        message =new ActiveMQTextMessage();
        message.setText(jsonString1);

    }

    @Test
    public void testListner() throws Exception {
        when(env.getProperty(any())).thenReturn("DECISION_SERVICE_ID");
        when(expectationCache.get(any())).thenReturn(new Expectation("10002100800001020230223050340","PENDING",30));
        when(activeMQConnectionFactory.createConnection()).thenReturn(connection);
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession()).thenReturn(session);
        when(session.createQueue(any())).thenReturn(queue);
        when(session.createProducer(any())).thenReturn(messageProducer);
        when(session.createBytesMessage()).thenReturn(bytesMessage);
        Assert.assertTrue(listener.consumeLogic(message,"mosip-to-adjudication"));
    }
}