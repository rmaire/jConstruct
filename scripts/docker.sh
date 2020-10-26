apt-get -y update
apt-get -y upgrade
apt-get -y install apt-transport-https ca-certificates curl gnupg-agent software-properties-common make dkms build-essential bsdtar curl wget git-core unzip

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

apt-get -y update
apt-get -y install docker-ce docker-ce-cli containerd.io

systemctl stop docker

echo "DOCKER_OPTS='-H tcp://0.0.0.0:4243 -H unix:///var/run/docker.sock'" | sudo tee --append /etc/environment

sed -i "s/ExecStart=\/usr\/bin\/dockerd -H fd:\/\//ExecStart=\/usr\/bin\/dockerd -H fd:\/\/ -H tcp:\/\/0.0.0.0:4243/" /lib/systemd/system/docker.service

usermod -aG docker vagrant

systemctl daemon-reload
systemctl start docker
systemctl enable docker
systemctl reload docker

docker network create --driver bridge testnet

docker run -d -p 5432:5432 \
  -e POSTGRES_PASSWORD=pw4test \
  --restart always \
  --hostname testdb \
  --name testdb postgres:9.5.19

sleep 5

docker network connect testnet testdb

docker run -it --rm --network testnet -e PGPASSWORD=pw4test postgres:9.5.19  psql -w -h testdb -U postgres -c "CREATE USER jirauser PASSWORD 'pw4test'"
docker run -it --rm --network testnet -e PGPASSWORD=pw4test postgres:9.5.19  psql -w -h testdb -U postgres -c "CREATE DATABASE jiradb WITH ENCODING 'UNICODE' LC_COLLATE 'C' LC_CTYPE 'C' TEMPLATE template0"
docker run -it --rm --network testnet -e PGPASSWORD=pw4test postgres:9.5.19  psql -w -h testdb -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE jiradb TO jirauser"

docker run -d -p 8080:8080 \
-e X_PROXY_NAME="mycloud.vm" \
--restart always \
--hostname mycloud.vm \
--name jira zuara/jira:8.3.1

docker network connect testnet jira
