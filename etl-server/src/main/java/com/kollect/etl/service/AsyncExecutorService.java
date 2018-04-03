package com.kollect.etl.service;

import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kollect.etl.entity.TransactionLoad;
import com.kollect.etl.util.LogStats;

@Service
public class AsyncExecutorService implements IAsyncExecutorService {
  
  private static final Logger LOG = LoggerFactory.getLogger(AsyncExecutorService.class);
  
  IReadWriteServiceProvider rwServiceProvider;
  IAsyncBatchService asyncService;
  
  AsyncExecutorService(IReadWriteServiceProvider rwServiceProvider, IAsyncBatchService asyncService){
   this.rwServiceProvider = rwServiceProvider; 
   this.asyncService = asyncService;
  }
  
  
  /* (non-Javadoc)
   * @see com.kollect.etl.service.IAsyncExecutorService#invoke(java.util.List, java.lang.String, int, int)
   */
  @Override
  public <T> void invoke (List<T> list, final String updateQuery, final int thread, final int commitSize ) {
    Iterator<T> itr = list.iterator();
    asyncService.execute(itr, new IRunnableProcess<TransactionLoad>() {
      @Override
      public void process(List<TransactionLoad> threadData) {
        long queryStart = System.currentTimeMillis();
        try (final SqlSession sqlSession = rwServiceProvider.getBatchSqlSession();) {
          for (int i = 0; i < threadData.size(); i++) {
            sqlSession.update(updateQuery, threadData.get(i));
          }
          sqlSession.commit();
          long queryEnd = System.currentTimeMillis();
          LogStats.logQueryStatistics("parallelStream", updateQuery, queryStart, queryEnd);
        } catch (PersistenceException persEx) {
          LOG.error("Failed to execute update statement: {}", updateQuery, persEx.getCause());
          throw persEx;
        }
      }
    }, thread, commitSize);
  }

}
