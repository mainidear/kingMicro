package com.king.services.spi;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.king.api.smp.ScheduleJobService;
import com.king.common.annotation.DynamicColFilter;
import com.king.common.utils.Page;
import com.king.common.utils.constant.Constant;
import com.king.dal.gen.model.smp.ScheduleJob;
import com.king.dal.gen.model.smp.ScheduleJobLog;
import com.king.dal.gen.service.BaseServiceImpl;
import com.king.dao.ScheduleJobDao;
import com.king.dao.ScheduleJobLogDao;
import com.king.utils.ScheduleUtils;

@Service("scheduleJobService")
public class ScheduleJobServiceImpl extends BaseServiceImpl<ScheduleJob>implements ScheduleJobService {
	@Autowired
    private Scheduler scheduler;
	@Autowired
	private ScheduleJobDao schedulerJobDao;
	@Autowired
	private ScheduleJobLogDao scheduleJobLogDao;
	/**
	 * spring容器启动后，初始化定时器
	 */
	@PostConstruct
	public void init(){
		List<ScheduleJob> scheduleJobList = schedulerJobDao.queryList(new HashMap<String, Object>());
		for(ScheduleJob scheduleJob : scheduleJobList){
			CronTrigger cronTrigger = ScheduleUtils.getCronTrigger(scheduler, scheduleJob.getJobId());
            //如果不存在，则创建
            if(cronTrigger == null) {
                ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
            }else {
                ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);
            }
		}
	}
	
	@Override
	@Transactional
	public void save(ScheduleJob scheduleJob) {
		scheduleJob.setCreateTime(new Date());
		scheduleJob.setStatus(Constant.ScheduleStatus.NORMAL.getValue());
        schedulerJobDao.save(scheduleJob);     
        ScheduleUtils.createScheduleJob(scheduler, scheduleJob);
    }
	
	@Override
	@Transactional
	public void update(ScheduleJob scheduleJob) {
        ScheduleUtils.updateScheduleJob(scheduler, scheduleJob);            
        schedulerJobDao.update(scheduleJob);
    }

	@Override
	@Transactional
    public void deleteBatch(Object[] jobIds) {
    	for(Object jobId : jobIds){
    		ScheduleUtils.deleteScheduleJob(scheduler, jobId);
    	}  	
    	//删除数据
    	schedulerJobDao.deleteBatch(jobIds);
	}

	@Override
    public int updateBatch(Object[] jobIds, Boolean status){
    	Map<String, Object> map = new HashMap<>();
    	map.put("list", jobIds);
    	map.put("status", status);
    	return schedulerJobDao.updateBatch(map);
    }
    
	@Override
	@Transactional
    public void run(Object[] jobIds) {
    	for(Object jobId : jobIds){
    		ScheduleUtils.run(scheduler, queryObject(jobId));
    	}
    }

	@Override
	@Transactional
    public void pause(Object[] jobIds) {
        for(Object jobId : jobIds){
    		ScheduleUtils.pauseJob(scheduler, jobId);
    	}
        
    	updateBatch(jobIds, Constant.ScheduleStatus.PAUSE.getValue());
    }

	@Override
	@Transactional
    public void resume(Object[] jobIds) {
    	for(Object jobId : jobIds){
    		ScheduleUtils.resumeJob(scheduler, jobId);
    	}
    	updateBatch(jobIds, Constant.ScheduleStatus.NORMAL.getValue());
    }

	@Transactional(readOnly = true)
	public ScheduleJobLog queryScheduleJobLog(Object jobId) {
		return scheduleJobLogDao.queryObject(jobId);
	}

	@Transactional(readOnly = true)
	public List<ScheduleJobLog> queryScheduleJobLogList(Map<String, Object> map) {
		return scheduleJobLogDao.queryList(map);
	}

	@Transactional(readOnly = true)
	public int queryScheduleJobLogTotal(Map<String, Object> map) {
		return scheduleJobLogDao.queryTotal();
	}

	@Override
	public void save(ScheduleJobLog log) {
		scheduleJobLogDao.save(log);
	}
	
	@Transactional(readOnly = true)
	@DynamicColFilter
	public Page getPageScheduleJobLog(Map<String, Object> map) {
		Page page = null;
		if (map.get("limit") != null && map.get("page") != null) {
			List<ScheduleJobLog> list = scheduleJobLogDao.queryList(map);
			int totalCount = scheduleJobLogDao.queryTotal(map);
			page = new Page(list, totalCount, (int) map.get("limit"), (int) map.get("page"));
		}
		return page;
	}

	@Override
	public int saveBatch(List<ScheduleJobLog> list) {
		
		return scheduleJobLogDao.saveBatch(list);
	}
    
}
