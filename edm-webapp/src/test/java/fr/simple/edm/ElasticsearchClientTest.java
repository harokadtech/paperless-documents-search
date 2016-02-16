package fr.simple.edm;

import static org.fest.assertions.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ComponentScan(basePackages = { "fr.simple.edm" })
public class ElasticsearchClientTest {

    @Autowired
    private ElasticsearchTestingHelper elasticsearchTestingHelper;
    
    @Autowired
    public ElasticsearchConfig elasticsearchConfig;
    
    /**
     * Will destroy and rebuild ES_INDEX
     */
    @Before
    public void setUp() throws Exception {
        elasticsearchTestingHelper.destroyAndRebuildIndex(ElasticsearchTestingHelper.ES_INDEX_DOCUMENTS);
    }
    
    @Test
    public void localNodeShouldBeStartedAndWorking() throws Exception {
        Field clientField = ElasticsearchConfig.class.getDeclaredField("elasticsearchClient");
        clientField.setAccessible(true);

        Client client = (Client) clientField.get(elasticsearchConfig);

        assertThat(client).isNotNull();
        
        CountResponse response = client.prepareCount(ElasticsearchTestingHelper.ES_INDEX_DOCUMENTS).execute().actionGet();
        assertThat(response.getCount()).isEqualTo(0);
    }
    
}
