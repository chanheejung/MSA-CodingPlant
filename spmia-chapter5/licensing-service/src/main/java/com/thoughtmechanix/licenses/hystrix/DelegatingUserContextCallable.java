package com.thoughtmechanix.licenses.hystrix;

import com.thoughtmechanix.licenses.utils.UserContext;
import com.thoughtmechanix.licenses.utils.UserContextHolder;
import java.util.concurrent.Callable;


public final class DelegatingUserContextCallable<V> implements Callable<V> {
    private final Callable<V> delegate;
    private UserContext originalUserContext;

    /** "사용자 정의 Callable 클래스"에 "히스트릭스로 보호된 코드"를 호출하는
     *  "원본 Callable 클래스"와 "부모 스레드에서 받은 UserContext"를 전달한다.*/
    public DelegatingUserContextCallable(Callable<V> delegate,
                                             UserContext userContext) {
        this.delegate = delegate;
        this.originalUserContext = userContext;
    }

    /** @HystrixCommand 애너테이션이 메서드를 보호하기 전에
     *   호출되는 Call() 함수다. */
    public V call() throws Exception {
        /** UserContext가 설정된다.
         *   UserContext를 저장하는 ThreadLocal 변수는
         *   히스트릭스가 보호하는 메소드를 실행하는 스레드에 연결된다. */
        UserContextHolder.setContext( originalUserContext );

        try {
            /** UserContext가 설정되면 LicenseService.getLicensesByOrg() 같은
             *   히스트릭스가 보호하는 메서드의 call() 메서드를 호출한다. */
            return delegate.call();
        }
        finally {
            this.originalUserContext = null;
        }
    }

    public static <V> Callable<V> create(Callable<V> delegate,
                                         UserContext userContext) {
        return new DelegatingUserContextCallable<V>(delegate, userContext);
    }
}