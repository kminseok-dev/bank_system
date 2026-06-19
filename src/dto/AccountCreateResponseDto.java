package dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@NoArgsConstructor
public class AccountCreateResponseDto {
	private Long accountId;       // DB에 저장된 계좌 고유 PK
    private Long memberId;        // 소유자 ID
    private String customerName;  // 고객명
    private String accountNumber; // 생성된 계좌 번호 (213 + 랜덤9자리)
    private String accountName;   // 계좌 별칭
    private Long balance;         // 잔액
    private String createdAt;     // 개설 일시
}
