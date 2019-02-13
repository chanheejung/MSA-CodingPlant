package com.thoughtmechanix.licenses.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.clients.OrganizationRestTemplateClient;
import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.LicenseRepository;
import com.thoughtmechanix.licenses.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class LicenseService {
    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);
    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config;

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;


    public License getLicense(String organizationId,String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = getOrganization(organizationId);

        return license
                .withOrganizationName( org.getName())
                .withContactName( org.getContactName())
                .withContactEmail( org.getContactEmail() )
                .withContactPhone( org.getContactPhone() )
                .withComment(config.getExampleProperty());
    }

    @HystrixCommand
    private Organization getOrganization(String organizationId) {
        return organizationRestClient.getOrganization(organizationId);
    }

    private void randomlyRunLong(){
      Random rand = new Random();

      int randomNum = rand.nextInt((3 - 1) + 1) + 1;

      if (randomNum==3) sleep();
    }

    private void sleep(){
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @HystrixCommand(
            fallbackMethod = "buildFallbackLicenseList",
            // 스레드 풀의 고유 이름 지정
            threadPoolKey = "licenseByOrgThreadPool",
            // 스레드 풀 속성으로 동작 정의 및 설정
            threadPoolProperties =
                    // 스레드 풀의 갯수를 정의
                    {@HystrixProperty(name = "coreSize",value="30"),
                     // 스레드 풀 앞에 배치할 큐와 큐에 넣을 요청 수를 정의
                     //  => 스레드가 분주할 때 큐 이용
                     @HystrixProperty(name="maxQueueSize", value="10")},
            commandProperties={
                     // Timeout 설정 12초
                     // @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="12000"),
                     // 히스트릭스가 호출 차단을 고려하는데 필요한 시간
                     @HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="10"),
                     // 호출 차단 실패 비율
                     @HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="75"),
                     // 차단 후 서비스의 회복 상태를 확인할 때까지 대기할 시간
                     @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="7000"),
                     // 서비스 호출 문제를 모니터할 시간 간격을 설정
                     @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds", value="15000"),
                     // 설정한 시간 간격 동안 통계를 수집할 횟수를 설정
                     @HystrixProperty(name="metrics.rollingStats.numBuckets", value="5")}
    )
    public List<License> getLicensesByOrg(String organizationId){
        logger.debug("LicenseService.getLicensesByOrg  Correlation id: {}", UserContextHolder.getContext().getCorrelationId());

        /** DB 호출 3회중 랜덤 1회 11초 지연 */
        randomlyRunLong();

        return licenseRepository.findByOrganizationId(organizationId);
    }

    private List<License> buildFallbackLicenseList(String organizationId){
        List<License> fallbackList = new ArrayList<>();
        License license = new License()
                .withId("0000000-00-00000")
                .withOrganizationId( organizationId )
                .withProductName("Sorry no licensing information currently available");

        fallbackList.add(license);
        return fallbackList;
    }

    public void saveLicense(License license){
        license.withId( UUID.randomUUID().toString());

        licenseRepository.save(license);
    }

    public void updateLicense(License license){
      licenseRepository.save(license);
    }

    public void deleteLicense(License license){
        licenseRepository.delete(license);
    }

}
