package com.bot.health.service;

import com.bot.health.config.AuthenticationProvider;
import com.bot.health.documentloader.OCIDocumentLoader;
import com.bot.health.embeddingmodel.OCIEmbeddingModel;
import com.bot.health.splitter.LineSplitter;
import com.bot.health.vectorstore.OracleVectorStore;
import com.bot.health.workflow.ChatWorkflow;
import com.bot.health.workflow.EmbeddingWorkflow;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.nio.file.Paths;
import java.sql.SQLException;
import static com.bot.health.service.OCIChatService.InferenceRequestType.COHERE;


@Service
public class OCIBotService {


    private final String compartmentId;

    private final String chatModelId;

    private final String embeddingModelId;

    private final String namespace;

    private final String bucketName;

    private final String objectPrefix;

    private final int vectorDimensions;

    private final String tableName;

    private final AuthenticationDetailsProvider authProvider;

    private final OracleVectorStore vectorStore;

    private final OCIDocumentLoader documentLoader;

    private final OCIEmbeddingModel ociEmbeddingModel;
    private final EmbeddingWorkflow embeddingWorkflow;
    private final OCIChatService ociChatService;

    public OCIBotService(@Value("${oci.configFileName}") final String configFileName,
                         @Value("${oci.compartmentId}") String compartmentId,
                         @Value("${oci.chatModelId}") String chatModelId,
                         @Value("${oci.embeddingModelId}") String embeddingModelId,
                         @Value("${oci.namespace}") String namespace,
                         @Value("${oci.bucketName}") String bucketName,
                         @Value("${oci.objectPrefix}") String objectPrefix,
                         @Value("${oci.vectorDimensions}") int vectorDimensions,
                         @Value("${oci.tableName}")  String tableName
                         ){

        this.compartmentId = compartmentId;
        this.chatModelId = chatModelId;
        this.embeddingModelId = embeddingModelId;
        this.namespace = namespace;
        this.bucketName = bucketName;
        this.objectPrefix = objectPrefix;
        this.vectorDimensions = vectorDimensions;
        this.tableName = tableName;

        try{
            this.authProvider = new ConfigFileAuthenticationDetailsProvider(
                    Paths.get(configFileName).toString(),
                    "DEFAULT"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.vectorStore = createVectorStore();
        this.documentLoader = documentLoader();
        this.ociEmbeddingModel = ociEmbeddingModel();
        this.embeddingWorkflow = embeddingWorkflow();
        embeddingWorkflow.run();
        this.ociChatService = ociChatService();
    }

    public void load(){
        embeddingWorkflow.run();
    }

    public void dropTableAndLoad(){
        vectorStore.dropTable();
        vectorStore.createTableIfNotExists();
        embeddingWorkflow.run();
    }

    public OracleVectorStore createVectorStore(){
        try{
            DataSource ds = testContainersDataSource();
            OracleVectorStore vectorStore = new OracleVectorStore(
                    ds,
                    tableName,
                    vectorDimensions
            );
            vectorStore.createTableIfNotExists();

            return vectorStore;
        }catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    public OCIDocumentLoader documentLoader(){
        try{
            return new OCIDocumentLoader(
                    ObjectStorageClient.builder().build(authProvider),
                    namespace
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OnDemandServingMode onDemandServingMode(String modelId){
        return  OnDemandServingMode.builder()
                .modelId(modelId)
                .build();
    }

    public OCIEmbeddingModel ociEmbeddingModel(){
        return OCIEmbeddingModel.builder()
                .servingMode(onDemandServingMode(embeddingModelId))
                .aiClient(GenerativeAiInferenceClient.builder()
                        .build(authProvider))
                .compartmentId(compartmentId)
                .build();
    }


    public OCIChatService ociChatService(){
        return OCIChatService.builder()
                .authProvider(authProvider)
                .servingMode(onDemandServingMode(chatModelId))
                .inferenceRequestType(COHERE)
                .compartment(compartmentId)
                .build();
    }

    public EmbeddingWorkflow embeddingWorkflow(){
        return EmbeddingWorkflow.builder()
                .vectorStore(vectorStore)
                .embeddingModel(ociEmbeddingModel)
                .documentLoader(documentLoader)
                .splitter(new LineSplitter())
                .namespace(namespace)
                .bucketName(bucketName)
                .objectPrefix(objectPrefix)
                .build();
    }

    public ChatWorkflow chatWorkflow(String userQuestion){

        String promptTemplate = """
                You are an assistant for question-answering tasks. Use the following pieces of retrieved context to answer the question. If you don't know the answer, just say that you don't know. Use three sentences maximum and keep the answer concise.
                Question: {%s}
                Context: {%s}
                Answer:
                """;

        return ChatWorkflow.builder()
                .vectorStore(vectorStore)
                .chatService(ociChatService)
                .embeddingModel(ociEmbeddingModel)
                .minScore(0.7)
                .promptTemplate(promptTemplate)
                .userQuestion(userQuestion)
                .build();
    }

    private DataSource testContainersDataSource() throws SQLException {
        // Configure a datasource for the Oracle container.

        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");

        dataSource.setConnectionPoolName("VECTOR_SAMPLE");
        dataSource.setUser("ADMIN");
        dataSource.setPassword("Aytida@12345");
        dataSource.setURL("jdbc:oracle:thin:@chatbotdatabase_medium?TNS_ADMIN=src/main/resources/Wallet_ChatbotDatabase/");
        dataSource.setInitialPoolSize(5);
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(30);
        dataSource.setTimeoutCheckInterval(5);
        dataSource.setInactiveConnectionTimeout(10);

        return dataSource;

    }
}
