package com.example.springboot.controller;

import cn.hutool.core.util.RandomUtil;
import com.example.springboot.common.annotation.AutoLog;
import cn.hutool.core.date.DateUtil;
import com.example.springboot.entity.User;
import com.example.springboot.utils.SessionUtils;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.entity.Goods;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author zlw
* @since 2024-02-05
*/
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private IGoodsService goodsService;

    @AutoLog("新增积分商品")
    @PostMapping
    public Result save(@RequestBody Goods goods) {
        goods.setCode("G"+ RandomUtil.randomNumbers(8));
        goodsService.save(goods);
        return Result.success();
    }

    @AutoLog("编辑积分商品")
    @PutMapping
    public Result update(@RequestBody Goods goods) {
        goodsService.updateById(goods);
        return Result.success();
    }

    @AutoLog("删除积分商品")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        goodsService.removeById(id);
        return Result.success();
    }

    @AutoLog("批量删除积分商品")
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        goodsService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    public Result findAll() {
        return Result.success(goodsService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(goodsService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<Goods>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(goodsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Goods> list = goodsService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Goods信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

    }

    /**
    * excel 导入
    * @param file
    * @throws Exception
    */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Goods> list = reader.readAll(Goods.class);

        goodsService.saveBatch(list);
        return Result.success();
    }

}
