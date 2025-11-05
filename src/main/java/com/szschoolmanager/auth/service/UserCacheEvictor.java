package com.szschoolmanager.auth.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
public class UserCacheEvictor {

  @CacheEvict(value = "userDetails", key = "#username")
  public void evictUser(String username) {
      // aucun code : Spring s’en charge
  }
}
