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
