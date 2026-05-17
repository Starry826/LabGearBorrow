package com.starry.service;

import com.starry.mapper.BorrowMapper;
import com.starry.pojo.Borrow;
import com.starry.pojo.Result;
import com.starry.pojo.dto.AdminBorrowDTO;
import com.starry.pojo.dto.UserBorrowDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowService {
    @Autowired
    private BorrowMapper borrowMapper;

    public List<UserBorrowDTO> getAllByUserId(Integer id) {
        updateOverdueStatus();
        return borrowMapper.getAllByUserId(id);
    }

    public int cancel(Integer id) {
        return borrowMapper.cancel(id);
    }

    public List<AdminBorrowDTO> getAll() {
        updateOverdueStatus();
        return borrowMapper.getAll();
    }

    public int updateStatus(AdminBorrowDTO adminBorrowDTO) {
        //批准
        if (adminBorrowDTO.getStatus() == 2){
            LocalDateTime now = LocalDateTime.now();
            adminBorrowDTO.setBorrowTime(now);//借出时间
            adminBorrowDTO.setExpectedReturnTime(now.plusDays(60));//预计归还时间
            borrowMapper.updateAvailableQuantity(adminBorrowDTO.getEquipmentId(), -1);
            return borrowMapper.updateStatus(adminBorrowDTO);
        }
        //归还
        if (adminBorrowDTO.getStatus() == 3){
            adminBorrowDTO.setActualReturnTime(LocalDateTime.now());//实际归还时间
            borrowMapper.updateAvailableQuantity(adminBorrowDTO.getEquipmentId(), 1);
            return borrowMapper.updateStatus(adminBorrowDTO);
        }
        //拒绝
        if (adminBorrowDTO.getStatus() == 5){
            return borrowMapper.updateStatus(adminBorrowDTO);
        }
        return 0;
    }
    // 检查并更新逾期状态
    public void updateOverdueStatus() {
        // 获取所有借用记录
        List<AdminBorrowDTO> allBorrows = borrowMapper.getAll();

        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        for (AdminBorrowDTO borrow : allBorrows) {
            // 只检查状态为"借用中"(2)的记录
            if (borrow.getStatus() == 2 &&
                    borrow.getExpectedReturnTime() != null &&
                    borrow.getExpectedReturnTime().isBefore(now)) {

                // 更新为逾期状态
                AdminBorrowDTO updateDTO = new AdminBorrowDTO();
                updateDTO.setId(borrow.getId());
                updateDTO.setStatus(4); // 逾期状态

                borrowMapper.updateStatus(updateDTO);

            }
        }
    }
}
