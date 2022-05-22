package com.summer.manage.service.system;

import com.summer.common.core.BaseEntity;
import com.summer.common.core.BaseService;
import com.summer.common.exception.ThinkerException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.dao.system.SysPostDAO;
import com.summer.manage.dto.request.PostListRequest;
import com.summer.manage.dto.request.PostRequest;
import com.summer.manage.dto.response.SysPostResponse;
import com.summer.manage.entity.system.SysPost;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class PostService extends BaseService {

    @Inject
    private SysPostDAO sysPostDAO;

    public List<SysPost> selectPostAll() {
        return sysPostDAO.selectPostAll();
    }

    public List<Long> selectIdsListByUserId(Long userId) {
        return selectPostListByUserId(userId).stream().map(BaseEntity::getId).collect(Collectors.toList());
    }

    public List<SysPost> selectPostListByUserId(Long userId) {
        return sysPostDAO.selectPostListByUserId(userId);
    }

    public Pagination<SysPostResponse> listPost(PostListRequest request) {
        Pagination<SysPostResponse> pagination = Pagination.create(request.pager, request.size);
        List<SysPost> selectRoleList = sysPostDAO.selectPostList(request.postCode,
                                                                 request.postName,
                                                                 request.status,
                                                                 pagination.getOffset(),
                                                                 pagination.getSize());
        List<SysPostResponse> sysPostResponses = BeanHelper.castTo(selectRoleList, SysPostResponse.class);
        pagination.getList().addAll(sysPostResponses);
        Long total = sysPostDAO.selectPostListCount(request.postCode,
                                                    request.postName,
                                                    request.status);
        pagination.setTotal(null == total ? 0L : total);
        return pagination;
    }

    public SysPostResponse getInfo(Long id) {
        return BeanHelper.castTo(sysPostDAO.selectPostById(id), SysPostResponse.class);
    }

    public Integer add(PostRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkPostNameUnique(request))) {
            throw new ThinkerException(CodeMSG.PostRepeat);
        } else if (IConstant.Common.NOT_UNIQUE.equals(checkPostCodeUnique(request))) {
            throw new ThinkerException(CodeMSG.PostCodeRepeat);
        }
        SysPost sysPost = BeanHelper.castTo(request, SysPost.class);
        sysPost.createdBy = RequestContext.get().getSession().ext;
        return sysPostDAO.insertPost(sysPost);
    }

    private String checkPostCodeUnique(PostRequest request) {
        SysPost sysPost = sysPostDAO.checkPostCodeUnique(request.postCode);
        if (Objects.nonNull(sysPost) && sysPost.getId() != MathHelper.nvl(request.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    private String checkPostNameUnique(PostRequest request) {
        SysPost sysPost = sysPostDAO.checkPostNameUnique(request.postName);
        if (Objects.nonNull(sysPost) && sysPost.getId() != MathHelper.nvl(request.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    public Integer update(PostRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkPostNameUnique(request))) {
            throw new ThinkerException(CodeMSG.PostRepeat);
        } else if (IConstant.Common.NOT_UNIQUE.equals(checkPostCodeUnique(request))) {
            throw new ThinkerException(CodeMSG.PostCodeRepeat);
        }
        SysPost sysPost = BeanHelper.castTo(request, SysPost.class);
        sysPost.updatedBy = RequestContext.get().getSession().ext;
        return sysPostDAO.updatePost(sysPost);
    }

    public Integer del(Long[] postIds) {
        for (Long postId : postIds) {
            SysPost post = sysPostDAO.selectPostById(postId);
            if (sysPostDAO.countUserPostById(postId) > 0) {
                throw new ThinkerException(CodeMSG.Common.code(), post.postName + "岗位已分配，不能删除");
            }
        }
        return sysPostDAO.deletePostByIds(postIds);
    }
}
