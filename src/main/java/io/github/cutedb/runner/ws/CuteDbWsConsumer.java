package io.github.cutedb.runner.ws;

import flexjson.JSONSerializer;
import io.github.cutedb.runner.dto.Run;
import io.github.cutedb.runner.utils.DateTransformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by barmi83 on 29/07/16.
 */
@Component(ICuteDbWsConsumer.BEAN_ID)
public class CuteDbWsConsumer extends BaseWsConsumer implements ICuteDbWsConsumer{

    private static final Logger LOGGER = LoggerFactory.getLogger(CuteDbWsConsumer.class);
    private final String URL_GET_NEW_UUID = "runs/uuid";
    private final String URL_POST_NEW_RUN = "runs/uuid/";

    @Autowired(required = true)
    String cuteDbWsBaseUrl = "http://localhost:9000/";

    private boolean valideCuteDbWsBaseUrl() {
        if (StringUtils.isBlank(cuteDbWsBaseUrl)) {
            LOGGER.error("L'URL de récupération des révisions ADS n'est pas définie");
            return false;
        }

        return true;
    }

    public String generateRunUuid() {
        if (!valideCuteDbWsBaseUrl()) {
            return null;
        }
        String url = cuteDbWsBaseUrl + URL_GET_NEW_UUID;
        Response res = createAndFireGetRequest(new HashMap<>(), url);

        if (res.getStatus() != HttpStatus.OK.value()) {
            LOGGER.error("Une erreur est survenue dans la récupération d'un nouvel uuid");
            return null;
        }

        return readResponseAsString(res, url);

    }

    public Run createNewRun(Run newRun) {
        if (!valideCuteDbWsBaseUrl()) {
            return null;
        }
        String url = cuteDbWsBaseUrl + URL_POST_NEW_RUN + newRun.getUuid();

        String json = new JSONSerializer().exclude("*.class").transform(new DateTransformer(), Date.class).deepSerialize(newRun);
        Map<String, String> params = new HashMap<>();

        Response res = createAndFirePostRequest(params, url, json);
        return readResponse(Run.class, res, url);
    }
}
