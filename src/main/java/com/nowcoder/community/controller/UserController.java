package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path="/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path="/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String fileName=headerImage.getOriginalFilename();
        String suffix=fileName.substring(fileName.lastIndexOf('.'));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","图片格式错误");
            return "/site/setting";
        }

        //生成随机文件名
        fileName= CommunityUtil.generateUUID()+suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath+'/'+fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常",e);
        }

        //更新当前用户的头像路径（web路径）
        //domain+contextPath+ /user/header/xxx.jpg
        User user = hostHolder.getUser();
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        //服务器存放路径
        filename=uploadPath+'/'+filename;
        //文件后缀
        String suffix=filename.substring(filename.lastIndexOf('.'));
        //响应图片
        response.setContentType("image/"+suffix);
        try (
                OutputStream os=response.getOutputStream();
                FileInputStream fis= new FileInputStream(filename);
                )
        {
            byte[] buffer = new byte[1024];
            int p=0;
            while((p=fis.read(buffer))!=-1){
                os.write(buffer,0,p);
            }
        } catch (IOException e) {
            logger.error("读取头像失败："+e.getMessage());
        }

    }

    @RequestMapping(path = "/changePassword",method = RequestMethod.POST)
    public String changePassword(String oldPassword, String newPassword, Model model){
        //验证非空
        if(oldPassword==null){
            model.addAttribute("oldPasswordMsg","原密码不能为空");
            return "/site/setting";
        }
        if(newPassword==null){
            model.addAttribute("newPasswordMsg","新密码不能为空");
            return "/site/setting";
        }

        //验证原密码是否正确
        User user = hostHolder.getUser();
        String salt=user.getSalt();
        oldPassword=CommunityUtil.md5(oldPassword+salt);
        if(!oldPassword.equals(user.getPassword())){
            model.addAttribute("oldPasswordMsg","原密码错误");
            return "/site/setting";
        }

        //更改密码
        newPassword=CommunityUtil.md5(newPassword+salt);
        userService.updatePassword(user.getId(),newPassword);

        //强制登出
        return "redirect:/logout";
    }

    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("user not exist");
        }

        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //访问用户是否已关注当前查看的用户
        boolean hasFollowed = false;
        if(hostHolder.getUser() !=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }
}
