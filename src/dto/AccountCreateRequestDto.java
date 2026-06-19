package dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@NoArgsConstructor
public class AccountCreateRequestDto {
	private Long id;            // 회원 고유 ID
    private String customerName;// 고객명 
    private Long initialBalance;// 초기 잔액
    private String accountName; // 계좌이름
}
