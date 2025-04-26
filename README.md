# Impala JDBC Test

이 코드를 실행하기 위해서 Cloudera 에서 [Impala JDBC Download](https://www.cloudera.com/downloads/connectors/impala/jdbc/2-6-32.html) 다운로드 하도록 합니다.

## Build

```
# mvn -Dmaven.test.skip=true clean package
```

## Keytab 파일 생성

윈도 서버에서 keytab 파일을 생성할 수 있는 권한을 가진 사용자로 로그인 한 후 다음의 커맨드를 실행하여 keytab 파일을 생성합니다.

```
C:\Windows\System32\ktpass.exe -out cloudera.keytab -princ cloudera@DATALAKE.NET -mapuser cloudera -pass '1234567890' -ptype KRB5_NT_PRINCIPAL -crypto AES256-SHA1
```

## Keytab 파일에서 사용자 확인

다음의 커맨드를 이용해서 keytab 파일에서 사용자를 추출할 수 있습니다.

```
# klist -k -t -K myuser.keytab

Keytab name: FILE:myuser.keytab
KVNO Timestamp           Principal
---- ------------------- ------------------------------------
   1 2024-01-01 12:00:00 user1@EXAMPLE.COM (aes256-cts-hmac-sha1-96)
   1 2024-01-01 12:00:00 user1@EXAMPLE.COM (aes128-cts-hmac-sha1-96)
```

다음은 Python 코드로 작성한 코드입니다.

```python
import subprocess

def extract_principals_from_keytab(keytab_path):
    output = subprocess.check_output(["klist", "-k", keytab_path], encoding="utf-8")
    principals = []
    for line in output.splitlines():
        if "@" in line:
            principals.append(line.strip())
    return principals

print(extract_principals_from_keytab("/path/to/your.keytab"))
```
