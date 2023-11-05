# mqtt-with-quarkus

## EMQX Operator

[Office Operator](https://github.com/emqx/emqx-operator)

```bash
$ helm repo add jetstack https://charts.jetstack.io
$ helm repo update
$ helm upgrade --install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set installCRDs=true
```

```bash
$ helm upgrade --install emqx-operator emqx/emqx-operator  --namespace emqx-operator-system --create-namespace  --version 2.2.3
```
## PKI(Public Key Infrastructure)
假設公司為 Itachi。這邊會建立兩層的 PKI，其中包含 *Root CA* 和 *Intermediate CA*，*Intermediate CA* 將透過角色頒發 *End Entity Certificate *。顯示方式會如下
```
Root CA
Issuer: C=TW, O=Itachi Corp, OU=IC PKI, CN=Itachi Root CA
Subject: C=TW, O=Itachi Corp, OU=IC PKI, CN=Itachi Root CA

Intermediate CA
Issuer: C=TW, O=Itachi Corp, OU=IC PKI, CN=Itachi Root CA
Subject: C=TW, O=Itachi Corp, OU=IC PKI, CN=Itachi Issuing CA

End Entity Certificate 
Issuer: C=TW, O=Itachi Corp, OU=IC PKI, CN=Itachi Issuing CA
Subject: C=TW, O=Itachi Corp, OU=Tian Center, CN=Device Certificate
```
簽發 PKI 
1. 建立 Root Key
```bash
openssl genrsa -des3 -out root-ca.key 4096
# password 1234567890
```
2. 建立 root CA
```bash
$ openssl req -new -x509 -key root-ca.key -days 7300 -sha256 -extensions v3_ca -out ca.crt
Enter pass phrase for root-ca.key:
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [AU]:TW
State or Province Name (full name) [Some-State]:
Locality Name (eg, city) []:
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Itachi Corp
Organizational Unit Name (eg, section) []:IC PKI
Common Name (e.g. server FQDN or YOUR name) []:Itachi Root CA
Email Address []:
```

```bash=
$ openssl x509 -noout -text -in ca.pem
```

有使用 v3_ca，會看到憑證帶有 X509v3 擴充資訊，並顯示 X509v3 Basic Constraints 如下，表示這張憑證當作 CA 使用

```bash
 X509v3 extensions:
...
            X509v3 Basic Constraints: critical
                CA:TRUE
```

3. Intermediate  CA Key
```bash
openssl genrsa -des3 -out  intermediate-ca.key 4096
password a12345678
```
4. Intermediate  CA CSR
```bash
$ openssl req -sha256 -new -key intermediate-ca.key -out intermediate.csr
Enter pass phrase for intermediate-ca.key:
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [AU]:TW
State or Province Name (full name) [Some-State]:
Locality Name (eg, city) []:
Organization Name (eg, company) [Internet Widgits Pty Ltd]:Itachi Corp
Organizational Unit Name (eg, section) []:IC PKI
Common Name (e.g. server FQDN or YOUR name) []:Itachi Issuing CA
Email Address []:

Please enter the following 'extra' attributes
to be sent with your certificate request
A challenge password []:
An optional company name []:
```
5.Intermediate CA extension
```bash
vim intermediate.ext
[ intermediate_ca ]
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = CA:true, pathlen:0
```
6. 簽發 Intermediate CA
```bash
$ openssl x509 -req -in intermediate.csr -CA ca.crt -CAkey root-ca.key -CAserial ca.serial -CAcreateserial -days 730 -extensions intermediate_ca -extfile intermediate.ext -out intermediate-ca.crt
Certificate request self-signature ok
subject=C = TW, ST = Some-State, O = Itachi Corp, OU = IC PKI, CN = Itachi Issuing CA
Enter pass phrase for root-ca.key:
```
7. 驗證 Intermediate CA
```bash
$ openssl x509 -noout -text -in intermediate-ca.crt
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number:
            22:75:17:5c:20:43:12:ac:f9:0f:dd:4f:da:f3:6b:ae:53:c1:34:05
        Signature Algorithm: sha256WithRSAEncryption
        Issuer: C = TW, ST = Some-State, O = Itachi Corp, OU = IC PKI, CN = Itachi Root CA
        Validity
            Not Before: Nov  5 04:30:13 2023 GMT
            Not After : Nov  4 04:30:13 2025 GMT
        Subject: C = TW, ST = Some-State, O = Itachi Corp, OU = IC PKI, CN = Itachi Issuing CA
        Subject Public Key Info:
...
$ openssl verify -CAfile ca.crt intermediate-ca.crt
intermediate-ca.crt: OK
```
8. 
