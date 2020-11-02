package com.github.ollgei.tool.importing.dao.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.ollgei.tool.importing.dao.mapper.EmployeeMapper;
import com.github.ollgei.tool.importing.dao.entity.EmployeeEntity;
import com.github.ollgei.tool.importing.dao.service.EmployeeService;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, EmployeeEntity> implements EmployeeService {

}

