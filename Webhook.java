import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Webhook {
    public static void main(String[] args) {
        String prompt = System.getenv("LLM_PROMPT");
        String llmResult = useLLM(prompt);
        String template = System.getenv("LLM2_IMAGE_TEMPLATE");
        //String llmImageResult = useLLMForImage((template.formatted(llmResult)));
        // %s를 바탕으로 해당 개념을 이해할 수 있는 상징적 과정을 표현한 비유적 이미지를 만들어줘
        System.out.println("llmResult = " + llmResult);
        //System.out.println("llmImageResult = " + llmImageResult);
        String title = System.getenv("SLACK_WEBHOOK_TITLE");
        //sendSlackMessage(title, llmResult, llmImageResult);
        sendSlackMessage(title, llmResult);
    }

    public static String useLLM(String prompt) {
        // Gemini API 관련 환경변수
        String apiUrl = System.getenv("GEMINI_API_URL"); // "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
        String apiKey = System.getenv("GEMINI_API_KEY"); // Gemini API 키
        apiUrl = apiUrl + "?key=" + apiKey;
        // Gemini API 형식에 맞게 페이로드 구성
        String payload = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(prompt);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                // Gemini API는 쿼리 파라미터로 API 키를 전달하므로 Authorization 헤더 제거
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        String result = null;
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());

            if (response.statusCode() == 200) {
                // Gemini API 응답 구조에 맞게 파싱 변경
                // 예: {"candidates":[{"content":{"parts":[{"text":"응답 텍스트 내용"}]}}]}
                result = response.body().split("\"text\": \"")[1].split("\"")[0];
            } else {
                throw new RuntimeException("API 호출 실패: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    public static void sendSlackMessage(String title, String text) {
        String slackUrl = System.getenv("SLACK_WEBHOOK_URL"); // 환경변수로 관리
        String payload = """
                    {"attachments": [{
                        "title": "%s",
                        "text": "%s",
                    }]}
                """.formatted(title, text);
        // 마치 브라우저나 유저인 척하는 것.
        HttpClient client = HttpClient.newHttpClient(); // 새롭게 요청할 클라이언트 생성
        // 요청을 만들어보자! (fetch)
        HttpRequest request = HttpRequest.newBuilder()
                // 어디로? URI(URL) -> Uniform Resource Identifier(Link)
                .uri(URI.create(slackUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(); // 핵심

        // 네트워크 과정에서 오류가 있을 수 있기에 선제적 예외처리
        try { // try
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            // 2는 뭔가 됨. 4,5 뭔가 잘못 됨. 1,3? 이런 건 없어요. 1은 볼 일이 없고요. 3은... 어...
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());
        } catch (Exception e) { // catch exception e
            throw new RuntimeException(e);
        }
    }
}



// 기존 이미지 코드
// import java.net.URI;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;

// public class Webhook {
//     public static void main(String[] args) {
//         String prompt = System.getenv("LLM_PROMPT");
//         String llmResult = useLLM(prompt);
//         System.out.println("llmResult = " + llmResult);
//         String template = System.getenv("LLM2_IMAGE_TEMPLATE");
//         String imagePrompt = template.formatted(llmResult);
//         System.out.println("imagePrompt = " + imagePrompt);
//         String llmImageResult = useLLMForImage(imagePrompt);
//         System.out.println("llmImageResult = " + llmImageResult);
//         String title = System.getenv("SLACK_WEBHOOK_TITLE");
//         sendSlackMessage(title, llmResult, llmImageResult);
//     }

//     public static String useLLMForImage(String prompt) {
//         String apiUrl = System.getenv("LLM2_API_URL");
//         String apiKey = System.getenv("LLM2_API_KEY");
//         String model = System.getenv("LLM2_MODEL");
//         String payload = """
//                 {
//                   "prompt": "%s",
//                   "model": "%s",
//                   "width": 1440,
//                   "height": 1440,
//                   "steps": 4,
//                   "n": 1
//                 }
//                 """.formatted(prompt, model);
//         HttpClient client = HttpClient.newHttpClient();
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(apiUrl))
//                 .header("Content-Type", "application/json")
//                 .header("Authorization", "Bearer " + apiKey)
//                 .POST(HttpRequest.BodyPublishers.ofString(payload))
//                 .build();
//         String result = null;
//         try {
//             HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//             System.out.println("response.statusCode() = " + response.statusCode());
//             System.out.println("response.body() = " + response.body());
//             result = response.body().split("url\": \"")[1].split("\",")[0];
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//         return result;
//     }

//     public static String useLLM(String prompt) {
//         String apiUrl = System.getenv("LLM_API_URL");
//         String apiKey = System.getenv("LLM_API_KEY");
//         String model = System.getenv("LLM_MODEL");
//         String payload = """
//                 {
//                   "messages": [
//                     {
//                       "role": "user",
//                       "content": "%s"
//                     }
//                   ],
//                   "model": "%s"
//                 }
//                 """.formatted(prompt, model);
//         HttpClient client = HttpClient.newHttpClient();
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(apiUrl))
//                 .header("Content-Type", "application/json")
//                 .header("Authorization", "Bearer " + apiKey)
//                 .POST(HttpRequest.BodyPublishers.ofString(payload))
//                 .build();
//         String result = null;
//         try {
//             HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//             System.out.println("response.statusCode() = " + response.statusCode());
//             System.out.println("response.body() = " + response.body());
//             result = response.body().split("\"content\":\"")[1].split("\"},\"logprobs\"")[0];
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//         return result;
//     }

//     public static void sendSlackMessage(String title, String text, String imageUrl) {
//         String slackUrl = System.getenv("SLACK_WEBHOOK_URL");
//         String payload = """
//                     {"attachments": [{
//                         "title": "%s",
//                         "text": "%s",
//                         "image_url": "%s"
//                     }]}
//                 """.formatted(title, text, imageUrl);
//         HttpClient client = HttpClient.newHttpClient();
//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(slackUrl))
//                 .header("Content-Type", "application/json")
//                 .POST(HttpRequest.BodyPublishers.ofString(payload))
//                 .build();
//         try {
//             HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//             System.out.println("response.statusCode() = " + response.statusCode());
//             System.out.println("response.body() = " + response.body());
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//     }
// }