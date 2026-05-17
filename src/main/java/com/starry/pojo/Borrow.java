package com.starry.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrow {
    private Integer id;
    private Integer userId;
    private Integer equipmentId;
    private LocalDateTime borrowTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime;
    private Integer status;
    private LocalDateTime updated_at;
}
