#This script is used automate the process of creating the live ISO.

#!/bin/bash


set -e

echo "Turning on all repositories for apt..."
sed -Ei 's/^# deb-src /deb-src /' /etc/apt/sources.list
if [[ $? -ne 0 ]]; then
    echo "Failed to turn on all repositories" >>/dev/stderr
    exit 1
fi

echo "Installing all apt dependencies..."
apt update && \
    apt -y install build-essential autoconf libtool automake git zip wget ant \
        libde265-dev libheif-dev \
        libpq-dev \
        testdisk libafflib-dev libewf-dev libvhdi-dev libvmdk-dev \
        libgstreamer1.0-0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad \
        gstreamer1.0-plugins-ugly gstreamer1.0-libav gstreamer1.0-tools gstreamer1.0-x \
        gstreamer1.0-alsa gstreamer1.0-gl gstreamer1.0-gtk3 gstreamer1.0-qt5 gstreamer1.0-pulseaudio

if [[ $? -ne 0 ]]; then
    echo "Failed to install necessary dependencies" >>/dev/stderr
    exit 1
fi

echo "Installing bellsoft Java 8..."
pushd /usr/src/ &&
    wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | sudo apt-key add - &&
    echo "deb [arch=amd64] https://apt.bell-sw.com/ stable main" | sudo tee /etc/apt/sources.list.d/bellsoft.list &&
    apt update &&
    apt -y install bellsoft-java8-full &&
    popd
if [[ $? -ne 0 ]]; then
    echo "Failed to install bellsoft java 8" >>/dev/stderr
    exit 1
fi

#installing sleuthkit using the debian files
if [ "$1" != ""  ]; then
TSK_REGEX="\\s*sleuthkit-java_([0-9\\.]*)"
[[ $1 =~ $TSK_REGEX ]]
TSK_VERSION_NUM=${BASH_REMATCH[1]}
[ ! -f "./$1" ] && wget https://github.com/sleuthkit/sleuthkit/releases/download/sleuthkit-$TSK_VERSION_NUM/$1;
  apt-get -y install ./$1;
  rm ./$1
fi


installAutopsy () {
[ ! -f "./$1" ] && wget https://github.com/sleuthkit/autopsy/releases/download/${1%.*}/$1;
   [ ! -d "/${1%.*}" ] && unzip ./$1 -d /
   export JAVA_HOME=/usr/lib/jvm/bellsoft-java8-full-amd64 && cd /${1%.*} && sh unix_setup.sh && cd -
   rm ./$1 > /dev/null
 }


#installing autopsy using the zip files

if [ "$2" != "" ]; then
   installAutopsy $2;
fi

[ ! -f "./launch_script_bootable.sh" ] && wget https://raw.githubusercontent.com/sleuthkit/autopsy/develop/unix/launch_script_bootable.sh
mv launch_script_bootable.sh /${2%.*}/autopsy.sh
sed -i -e "s/\/usr\/share\/autopsy-4.7.0\/bin\/autopsy/\/${2%.*}\/bin\/autopsy/g" /${2%.*}/autopsy.sh
chmod +x /${2%.*}/autopsy.sh

ln -s /${2%.*}/autopsy.sh /usr/local/bin/autopsy

touch /usr/share/applications/autopsy.desktop

echo -e "[Desktop Entry]\nVersion=1.0\nName=Autopsy\nComment=Complete Digital forensics analysis suite\nExec=sudo /usr/local/bin/autopsy\nIcon=/usr/share/icons/autopsy.png\nTerminal=true\nType=Application\nCategories=Utility;System;" > /usr/share/applications/autopsy.desktop

chmod +x /usr/share/applications/autopsy.desktop

#setup desktop files
mkdir /etc/skel/Desktop
cp /usr/share/applications/autopsy.desktop /etc/skel/Desktop/
cp /usr/share/applications/xfce4-terminal.desktop /etc/skel/Desktop/

#setup autopsy icon
[ ! -f "./autopsy.png" ] && wget https://github.com/sleuthkit/autopsy/raw/develop/unix/autopsy.png
mv ./autopsy.png /usr/share/icons

#setup iso wallpaper
[ ! -f "./autopsy_wallpaper1.png" ] && wget https://github.com/sleuthkit/autopsy/raw/develop/unix/autopsy_wallpaper1.png
mv ./autopsy_wallpaper1.png /usr/share/xfce4/backdrops/autopsy_wallpaper.png
unlink /usr/share/xfce4/backdrops/xubuntu-wallpaper.png
ln -s /usr/share/xfce4/backdrops/autopsy_wallpaper.png /usr/share/xfce4/backdrops/xubuntu-wallpaper.png
