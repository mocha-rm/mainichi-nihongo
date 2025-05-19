package com.jhlab.mainichi_nihongo.domain.content;

import org.springframework.stereotype.Service;

@Service
public class ContentService {
    /**
     * 오늘의 일본어 학습 컨텐츠를 생성합니다.
     *
     * @return HTML 형식의 일본어 학습 컨텐츠
     */
    public String generateDailyContent() {
        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.5;">
                    <h2 style="color: #4b0082;">🇯🇵 오늘의 일본어 표현</h2>
                
                    <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
                        <h3 style="color: #e91e63; margin-top: 0;">お疲れ様です (おつかれさまです)</h3>
                        <p style="font-size: 18px; margin-bottom: 5px;"><strong>발음:</strong> O-tsukare-sama desu</p>
                        <p style="font-size: 18px; margin-bottom: 5px;"><strong>의미:</strong> "수고하셨습니다" / "고생하셨습니다"</p>
                        <p style="font-size: 18px;"><strong>상황:</strong> 직장에서 동료나 부하 직원에게 인사할 때 사용하는 표현으로, 그들의 노고를 인정하고 감사를 표현합니다.</p>
                    </div>
                
                    <div style="background-color: #fff8e1; padding: 15px; border-radius: 5px; margin-bottom: 20px;">
                        <h3 style="color: #ff9800; margin-top: 0;">📝 예문</h3>
                        <p style="font-size: 18px; margin-bottom: 10px;">1. <span style="color: #0277bd;">今日も一日お疲れ様でした。</span></p>
                        <p style="font-size: 16px; margin-bottom: 20px;">Kyō mo ichinichi otsukaresama deshita. (오늘도 하루 수고하셨습니다.)</p>
                
                        <p style="font-size: 18px; margin-bottom: 10px;">2. <span style="color: #0277bd;">プロジェクトが終わりましたね。お疲れ様でした。</span></p>
                        <p style="font-size: 16px;">Purojekuto ga owarimashita ne. Otsukaresama deshita. (프로젝트가 끝났네요. 수고하셨습니다.)</p>
                    </div>
                
                    <div style="background-color: #e8f5e9; padding: 15px; border-radius: 5px;">
                        <h3 style="color: #388e3c; margin-top: 0;">💡 문화 노트</h3>
                        <p style="font-size: 16px;">
                            일본 직장 문화에서 '오쓰카레사마데스'는 매우 중요한 인사말입니다. 이 말은 상대방의 노력을 인정하고 
                            감사함을 표현하는 방식으로, 일본 사회의 상호 존중 문화를 잘 보여줍니다. 아침 인사로 '오하요 고자이마스'를 
                            사용하는 것과 달리, 하루 중이나 일과 후에는 '오쓰카레사마데스'를 사용하는 것이 일반적입니다.
                        </p>
                    </div>
                </div>
                """;
    }
}
