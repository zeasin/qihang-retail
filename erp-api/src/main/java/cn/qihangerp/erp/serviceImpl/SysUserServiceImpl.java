//package cn.qihangerp.erp.serviceImpl;
//
//import cn.qihangerp.model.entity.SysUser;
//import cn.qihangerp.service.ISysUserService;
//
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * 用户 业务层处理
// *
// * @author qihang
// */
//@Service
//public class SysUserServiceImpl implements ISysUserService
//{
//
//    @Override
//    public List<SysUser> selectUserList(SysUser user) {
//        return List.of();
//    }
//
//    @Override
//    public List<SysUser> selectAllocatedList(SysUser user) {
//        return List.of();
//    }
//
//    @Override
//    public List<SysUser> selectUnallocatedList(SysUser user) {
//        return List.of();
//    }
//
//    @Override
//    public SysUser selectUserByUserName(String userName) {
//        return null;
//    }
//
//    @Override
//    public SysUser selectUserById(Long userId) {
//        return null;
//    }
//
//    @Override
//    public boolean checkUserNameUnique(SysUser user) {
//        return false;
//    }
//
//    @Override
//    public boolean checkPhoneUnique(SysUser user) {
//        return false;
//    }
//
//    @Override
//    public boolean checkEmailUnique(SysUser user) {
//        return false;
//    }
//
//    @Override
//    public void checkUserAllowed(SysUser user) {
//
//    }
//
//    @Override
//    public int insertUser(SysUser user) {
//        return 0;
//    }
//
//    @Override
//    public boolean registerUser(SysUser user) {
//        return false;
//    }
//
//    @Override
//    public int updateUser(SysUser user) {
//        return 0;
//    }
//
//    @Override
//    public int updateUserStatus(SysUser user) {
//        return 0;
//    }
//
//    @Override
//    public int updateUserProfile(SysUser user) {
//        return 0;
//    }
//
//    @Override
//    public boolean updateUserAvatar(String userName, String avatar) {
//        return false;
//    }
//
//    @Override
//    public int resetPwd(SysUser user) {
//        return 0;
//    }
//
//    @Override
//    public int resetUserPwd(String userName, String password) {
//        return 0;
//    }
//
//    @Override
//    public int deleteUserById(Long userId) {
//        return 0;
//    }
//
//    @Override
//    public void checkUserDataScope(Long userId) {
//
//    }
//
//    @Override
//    public String selectUserRoleGroup(String userName) {
//        return "";
//    }
//
//    @Override
//    public void insertUserAuth(Long userId, Long[] roleIds) {
//
//    }
//}
