package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.SpuVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;

@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuListBySearchConditionsAndCategoryId(Long categoryId, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果categoryId不指定，则查询所有分类下的spu
        if(categoryId != 0){
            wrapper.eq("category_id",categoryId);
        }
        //用户搜索条件
        String key = pageParamVo.getKey();
        if(StringUtils.isBlank(key)){
            //函数式接口有四种：这里是消费型接口，里面用lambda表达式写出来消费一个接口即可
            //消费型接口有参数，没有返回值
            //供给型接口没参数，有返回值
            //功能型接口有参数，有返回值
            //判断型接口有参数，返回值是true/false
            wrapper.and(t->t.eq("id",key).or().like("name",key));
        }
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }

    @Override
    public void bigSave(SpuVo spu) {

        //1. 先保存spu相关信息

        //2. 再保存sku相关信息

        //3. 最后保存营销信息

    }

}