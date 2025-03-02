package org.example.delivery.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import org.example.delivery.pojo.DeliveryOrder;
import org.example.delivery.pojo.ProductDetail;
import org.example.delivery.pojo.WordInfo;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class OcrDataParser {

    private static final double ROW_TOLERANCE = 15.0; // 同一行的Y坐标容差
    private static final double COLUMN_GAP = 100.0; // 列间距阈值
    private static final ObjectMapper mapper = new ObjectMapper();
    public DeliveryOrder parseOcrResult(String ocrData) throws JsonProcessingException {


        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(ocrData);

        // 第一步：提取并解析data字段
        //JsonNode dataNode = parseDataField(root);
        // 第二步：提取文字信息
        //List<WordInfo> words = parseWordInfo(dataNode);

        // 关键修正点：data字段本身是字符串需要再次解析
        String dataString = root.get("data").asText();
        JsonNode dataNode = mapper.readTree(dataString);
        // 第二次解析
        JsonNode wordsInfo = dataNode.get("prism_wordsInfo");

        List<WordInfo> words = new ArrayList<>();
        for (JsonNode node : wordsInfo) {
            WordInfo info = new WordInfo();
            info.setWord(node.path("word").asText());
            info.setX(node.path("x").asDouble());
            info.setY(node.path("y").asDouble());
            words.add(info);
        }
        // 按Y坐标排序（从上到下）
        words.sort(Comparator.comparingDouble(WordInfo::getY));

        DeliveryOrder order = new DeliveryOrder();
        parseHeader(order, words);
        parseProducts(order, words);
        parseFooter(order, words);

        return order;
    }

    private JsonNode parseDataField(JsonNode root) throws JsonProcessingException {
        if (!root.has("data")) {
            System.out.println("缺少data字段");
        }

        String dataContent = root.get("data").asText();
        if (StringUtils.isBlank(dataContent)) {
            System.out.println("data字段内容为空");
        }

        return mapper.readTree(dataContent);
    }

    private List<WordInfo> parseWordInfo(JsonNode dataNode) {
        if (!dataNode.has("prism_wordsInfo")) {
            System.out.println("缺少prism_wordsInfo字段");
        }

        JsonNode wordsInfo = dataNode.get("prism_wordsInfo");
        List<WordInfo> words = new ArrayList<>();

        for (JsonNode node : wordsInfo) {
            WordInfo info = new WordInfo();
            info.setWord(node.path("word").asText());
            info.setX(node.path("x").asDouble());
            info.setY(node.path("y").asDouble());
            words.add(info);
        }

        return words;
    }

    private void parseHeader(DeliveryOrder order, List<WordInfo> words) {
        // 解析公司名称
        words.stream()
                .filter(w -> w.getWord().contains("公司"))
                .findFirst()
                .ifPresent(w -> order.setCompanyName(w.getWord()));

        // 解析开单日期
        words.stream()
                .filter(w -> w.getWord().startsWith("开单日期"))
                .findFirst()
                .ifPresent(w -> {
                    String date = w.getWord().replace("开单日期：", "");
                    order.setIssueDate(date);
                });

        // 解析单号
        words.stream()
                .filter(w -> w.getWord().startsWith("单号"))
                .findFirst()
                .ifPresent(w -> {
                    String num = w.getWord().replace("单号", "");
                    order.setOrderNumber(num);
                });
    }

    private void parseProducts(DeliveryOrder order, List<WordInfo> words) {
        List<ProductDetail> products = new ArrayList<>();
        List<WordInfo> productWords = words.stream()
                .filter(w -> w.getY() > 100 && w.getY() < 250) // 产品区域Y坐标范围
                .toList();

        // 按行分组
        Map<Double, List<WordInfo>> rows = productWords.stream()
                .collect(Collectors.groupingBy(
                        w -> Math.floor(w.getY() / ROW_TOLERANCE) * ROW_TOLERANCE
                ));

        for (List<WordInfo> row : rows.values()) {
            if (row.stream().anyMatch(w -> w.getWord().contains("螺纹钢") || w.getWord().contains("盘螺"))) {
                ProductDetail detail = new ProductDetail();

                // 按X坐标排序（从左到右）
                row.sort(Comparator.comparingDouble(WordInfo::getX));

                for (WordInfo word : row) {
                    double x = word.getX();

                    if (x < 100) {
                        detail.setProductName(word.getWord());
                    } else if (x < 200) {
                        detail.setSpecification(word.getWord());
                    } else if (x < 300) {
                        detail.setMaterial(word.getWord());
                    } else if (x < 400) {
                        parseDouble(word.getWord()).ifPresent(detail::setPieceWeightTon);
                    } else if (x < 500) {
                        parseInt(word.getWord()).ifPresent(detail::setQuantity);
                    } else if (x < 600) {
                        parseDouble(word.getWord()).ifPresent(detail::setTotalWeightTon);
                    }
                }

                if (detail.getProductName() != null) {
                    products.add(detail);
                }
            }
        }

        order.setProductDetails(products);
    }


    private void parseFooter(DeliveryOrder order, List<WordInfo> words) {
        // 解析签收人
        words.stream()
                .filter(w -> w.getWord().startsWith("签收人"))
                .findFirst()
                .ifPresent(w -> {
                    String receiver = w.getWord().replace("签收人：", "");
                    order.setReceiver(receiver);
                });

        // 解析发货地址
        words.stream()
                .filter(w -> w.getWord().startsWith("送货地址"))
                .findFirst()
                .ifPresent(w -> {
                    String address = w.getWord().replace("送货地址：", "");
                    order.setDeliveryAddress(address);
                });
    }

    private Optional<Double> parseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
