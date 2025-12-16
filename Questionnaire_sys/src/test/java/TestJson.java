import com.whu.survey.entity.Question;
import com.whu.survey.handler.JsonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

// 这是一个单元测试，不需要启动 Tomcat 就能跑
public class TestJson {

    @Test
    public void testJsonMock() throws Exception {
        // 模拟一个 Question 对象
        Question q = new Question();
        q.setTitle("测试音频题");
        q.setType("AUDIO");

        // 模拟前端传来的配置
        Map<String, Object> config = new HashMap<>();
        config.put("maxDuration", 60);
        config.put("placeholder", "请点击录音");
        q.setConfig(config);

        // 模拟 JsonTypeHandler 的工作
        ObjectMapper mapper = new ObjectMapper();
        String jsonSql = mapper.writeValueAsString(q.getConfig());

        System.out.println("准备存入数据库的 JSON: " + jsonSql);

        // 验证输出是否为: {"maxDuration":60,"placeholder":"请点击录音"}
        // 如果输出了这个 JSON 字符串，说明逻辑没问题！
    }
}