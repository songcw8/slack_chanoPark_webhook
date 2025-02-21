import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    public static String useLLMForImage(String prompt) {
        //black-forest-labs/FLUX.1-schnell-Free
        String apiUrl = System.getenv("LLM2_API_URL"); // 환경변수로 관리
        String apiKey = System.getenv("LLM2_API_KEY"); // 환경변수로 관리
        String model = System.getenv("LLM2_MODEL"); // 환경변수로 관리
        String payload = """
                {
                  "prompt": "%s",
                  "model": "%s",
                  "width": 1440,
                  "height": 1440,
                  "steps": 3,
                  "n": 1
                }
                """.formatted(prompt, model);
        HttpClient client = HttpClient.newHttpClient(); // 새롭게 요청할 클라이언트 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(); // 핵심
        String result = null;
        try { // try
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());
            result = response.body()
                    .split("url\": \"")[1]
                    .split("\",")[0];
        } catch (Exception e) { // catch exception e
            throw new RuntimeException(e);
        }
        return result; // 앞 뒤를 자르고 우리에게 필요한 내용만 리턴쓰.
    }


public static String useLLM(String prompt) {
    String apiKey = System.getenv("GEMINI_API_KEY"); // 환경변수로 관리
    String apiUrl = System.getenv("GEMINI_API_URL"); // 환경변수로 관리
    
    // 환경 변수 체크
    if (apiKey == null || apiKey.isEmpty()) {
        System.err.println("GEMINI_API_KEY 환경 변수가 설정되지 않았습니다.");
        return "API 키가 설정되지 않았습니다.";
    }
    
    if (apiUrl == null || apiUrl.isEmpty()) {
        System.err.println("GEMINI_API_URL 환경 변수가 설정되지 않았습니다.");
        // 기본 URL 설정
        apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
        System.err.println("기본 URL 사용: " + apiUrl);
    }

    if (!apiUrl.contains("?key=")) {
        apiUrl += "?key=" + apiKey;
    }
    
    String payload = String.format("""
            {
                "contents": [
                    {
                        "role": "user",
                        "parts": [
                            {
                                "text": "%s"
                            }
                        ]
                    }
                ]
            }
            """, prompt);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
            
    try {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("응답 상태 코드: " + response.statusCode());
        
        // 응답 상태 코드 확인
        if (response.statusCode() != 200) {
            System.out.println("API 호출 실패: " + response.statusCode());
            System.out.println("응답 내용: " + response.body());
            return "API 호출에 실패했습니다: " + response.statusCode();
        }
        
        String responseBody = response.body();
        if (responseBody == null || responseBody.isEmpty()) {
            System.out.println("응답 내용이 비어있습니다.");
            return "API 응답이 비어있습니다.";
        }
        
        // 정규식을 이용한 텍스트 추출
        String patternString = "\"text\":\\s*\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(responseBody);

        if (matcher.find()) {
            String result = matcher.group(1).trim()
                    .replace("\\n", "\n")  // 개행문자 처리
                    .replace("\\\"", "\""); // 따옴표 처리
            return result;
        } else {
            System.out.println("'text' 값을 찾을 수 없음!");
            System.out.println("응답 내용: " + responseBody);
            return "API 응답에서 'text' 값을 찾을 수 없습니다.";
        }
    } catch (Exception e) {
        e.printStackTrace();
        return "오류 발생: " + e.getMessage();
    }
}
    // public static String useLLM(String prompt) {
    //     String apiKey = System.getenv("GEMINI_API_KEY"); // 환경변수로 관리
    //     String apiUrl = System.getenv("GEMINI_API_URL"); // 환경변수로 관리
    //     // String model = System.getenv("LLM_API_MODEL"); // 환경변수로 관리

    //     if (!apiUrl.contains("?key=")) {
    //         apiUrl += "?key=" + apiKey;
    //     }
    //     String payload = String.format("""
    //             {
    //                 "contents": [
    //                     {
    //                         "role": "user",
    //                         "parts": [
    //                             {
    //                                 "text": "%s"
    //                             }
    //                         ]
    //                     }
    //                 ]
    //             }
    //             """, prompt);

    //     HttpClient client = HttpClient.newHttpClient(); // 새롭게 요청할 클라이언트 생성
    //     HttpRequest request = HttpRequest.newBuilder()
    //             .uri(URI.create(apiUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
    //             .header("Content-Type", "application/json")
    //             //.header("Authorization", "Bearer " + apiKey)
    //             .POST(HttpRequest.BodyPublishers.ofString(payload))
    //             .build(); // 핵심
    //     try { // try
    //         HttpResponse<String> response = client.send(request,
    //                 HttpResponse.BodyHandlers.ofString());
    //         // System.out.println("response.statsusCode() = " + response.statusCode());
    //         // System.out.println("response.body() = " + response.body());

    //         String responseBody = response.body();
    //         String result = null;
    //         // content 값이 시작하는 위치

    //         // ============= Gemini 문자열 파싱 ================ //
    //         String patternString = "\"text\":\\s*\"([^\"]+)\"";
    //         Pattern pattern = Pattern.compile(patternString);
    //         Matcher matcher = pattern.matcher(responseBody);

    //         if (matcher.find()) {
    //             return matcher.group(1).trim(); // ✅ 찾은 값 반환 (앞뒤 공백 제거)
    //         } else {
    //             System.out.println("'text' 값을 찾을 수 없음!");
    //             return "⚠ API 응답에서 'text' 값을 찾을 수 없음!";
    //         }

    //         /* 지금 Gemini
    //         "candidates": [
    //         {
    //           "content": {
    //             "parts": [
    //               {
    //                 "text": "무엇이든 기록하고 공유해봐요 📝.  코드는 간결하게, 주석은 명확하게!  괜찮아요, 질문 많이 하는 게 더 빨라요 👍.  그리고… 규칙적인 휴식 필수! ☕\n"
    //               }
    //             ],
    //             "role": "model"
    //           },
    //          */
    //         //result = responseBody.split("\"text\":")[1].split("\"")[0];
    //         //System.out.println("result = " + result);
    //         //return result;
    //     } catch (Exception e) { // 예외 처리
    //         throw new RuntimeException(e);
    //     }
    // }

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

    // public static String useLLM(String prompt) {
    //     // https://groq.com/
    //     // https://console.groq.com/playground
    //     // https://console.groq.com/docs/models -> production 을 권장 (사프나 포트폴리오 보자면...)
    //     // https://console.groq.com/docs/rate-limits -> 이중에서 왠지 일일 사용량 제한(RPD)이 빡빡한게 좋은 것일 확률이 높음
    //     // llama-3.3-70b-versatile -> 나중에 바뀔 가능성이 있다 없다? -> 환경변수로

    //     // 이름 바꾸기 -> 해당 메서드 내부? 클래스를 기준하다면 그 내부만 바꿔줌
    //     String apiUrl = System.getenv("LLM_API_URL"); // 환경변수로 관리
    //     String apiKey = System.getenv("LLM_API_KEY"); // 환경변수로 관리
    //     String model = System.getenv("LLM_MODEL"); // 환경변수로 관리
    //     String payload = """
    //             {
    //               "messages": [
    //                 {
    //                   "role": "user",
    //                   "content": "%s"
    //                 }
    //               ],
    //               "model": "%s"
    //             }
    //             """.formatted(prompt, model);
    //     HttpClient client = HttpClient.newHttpClient(); // 새롭게 요청할 클라이언트 생성
    //     HttpRequest request = HttpRequest.newBuilder()
    //             .uri(URI.create(apiUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
    //             .header("Content-Type", "application/json")
    //             .header("Authorization", "Bearer " + apiKey)
    //             .POST(HttpRequest.BodyPublishers.ofString(payload))
    //             .build(); // 핵심
    //     String result = null;
    //     try { // try
    //         HttpResponse<String> response = client.send(request,
    //                 HttpResponse.BodyHandlers.ofString());
    //         System.out.println("response.statusCode() = " + response.statusCode());
    //         System.out.println("response.body() = " + response.body());
    //         result = response.body()
    //                 .split("\"content\":\"")[1]
    //                 .split("\"},\"logprobs\"")[0];
    //     } catch (Exception e) { // catch exception e
    //         throw new RuntimeException(e);
    //     }
    //     return result; // 앞 뒤를 자르고 우리에게 필요한 내용만 리턴쓰.
    // }

//     public static void sendSlackMessage(String title, String text, String imageUrl) {
//         // 다시 시작된 슬랙 침공
// //        String slackUrl = "https://hooks.slack.com/services/";
//         String slackUrl = System.getenv("SLACK_WEBHOOK_URL"); // 환경변수로 관리
// //        String payload = "{\"text\": \"채널에 있는 한 줄의 텍스트입니다.\\n또 다른 한 줄의 텍스트입니다.\"}";
// //        String payload = "{\"text\": \"" + text + "\"}";
//         // slack webhook attachments -> 검색 혹은 LLM
//         String payload = """
//                     {"attachments": [{
//                         "title": "%s",
//                         "text": "%s",
//                         "image_url": "%s"
//                     }]}
//                 """.formatted(title, text, imageUrl);
//         // 마치 브라우저나 유저인 척하는 것.
//         HttpClient client = HttpClient.newHttpClient(); // 새롭게 요청할 클라이언트 생성
//         // 요청을 만들어보자! (fetch)
//         HttpRequest request = HttpRequest.newBuilder()
//                 // 어디로? URI(URL) -> Uniform Resource Identifier(Link)
//                 .uri(URI.create(slackUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
//                 .header("Content-Type", "application/json")
//                 .POST(HttpRequest.BodyPublishers.ofString(payload))
//                 .build(); // 핵심

//         // 네트워크 과정에서 오류가 있을 수 있기에 선제적 예외처리
//         try { // try
//             HttpResponse<String> response = client.send(request,
//                     HttpResponse.BodyHandlers.ofString());
//             // 2는 뭔가 됨. 4,5 뭔가 잘못 됨. 1,3? 이런 건 없어요. 1은 볼 일이 없고요. 3은... 어...
//             System.out.println("response.statusCode() = " + response.statusCode());
//             System.out.println("response.body() = " + response.body());
//         } catch (Exception e) { // catch exception e
//             throw new RuntimeException(e);
//         }
//     }
// }

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