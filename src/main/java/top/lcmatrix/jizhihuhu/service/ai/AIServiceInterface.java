package top.lcmatrix.jizhihuhu.service.ai;

import java.util.List;
import top.lcmatrix.jizhihuhu.model.AIResponse;
import top.lcmatrix.jizhihuhu.model.RoleContent;
public interface AIServiceInterface {

    AIResponse request(List<RoleContent> roleContents);
}
