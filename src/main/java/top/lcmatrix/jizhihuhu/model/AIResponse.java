package top.lcmatrix.jizhihuhu.model;

import lombok.Data;

@Data
public class AIResponse {
    boolean success;
    String error;
    String message = "";
}
