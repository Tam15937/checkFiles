
Name: file-damage-analyzer
Version: 1.0.0
Release: 1%{?dist}
Summary: Web service for analyzing damaged files in directories
Group: Applications/File

License: MIT
URL: https://github.com/yourusername/file-damage-analyzer
Source0: %{name}-%{version}.jar
Source1: %{name}.service
Source2: application.properties

BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root


Requires: java-21-openjdk
Requires(post): systemd
Requires(preun): systemd

%description
Web service for analyzing damaged files in directories.
Compares files between two directories and shows differences.
Provides web interface and REST API for analysis.

%prep


%build


%install

rm -rf %{buildroot}


mkdir -p %{buildroot}%{_sysconfdir}/%{name}
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_unitdir}
mkdir -p %{buildroot}/usr/share/%{name}
mkdir -p %{buildroot}/var/log/%{name}
mkdir -p %{buildroot}/var/lib/%{name}


install -m 644 %{SOURCE0} %{buildroot}/usr/share/%{name}/%{name}.jar


install -m 644 %{SOURCE2} %{buildroot}%{_sysconfdir}/%{name}/application.properties


cat > %{buildroot}%{_bindir}/%{name} << 'EOF'
#!/bin/bash
JAR_FILE="/usr/share/%{name}/%{name}.jar"
CONFIG_FILE="/etc/%{name}/application.properties"


if [ -f "$CONFIG_FILE" ]; then
    CONFIG_OPT="--spring.config.location=file:$CONFIG_FILE"
else
    echo "Warning: Configuration file not found at $CONFIG_FILE, using defaults"
    CONFIG_OPT=""
fi


exec java -jar "$JAR_FILE" $CONFIG_OPT "$@"
EOF
chmod 755 %{buildroot}%{_bindir}/%{name}


install -m 644 %{SOURCE1} %{buildroot}%{_unitdir}/%{name}.service

%clean
rm -rf %{buildroot}

%pre


if ! getent group %{name} >/dev/null; then
    groupadd -r %{name}
fi

if ! getent passwd %{name} >/dev/null; then
    useradd -r -g %{name} -s /sbin/nologin -d /var/lib/%{name} %{name}
fi

%post

mkdir -p /var/log/%{name}
mkdir -p /var/lib/%{name}
chown %{name}:%{name} /var/log/%{name}
chown %{name}:%{name} /var/lib/%{name}
chmod 755 /var/log/%{name}
chmod 755 /var/lib/%{name}


systemctl daemon-reload >/dev/null 2>&1 || :


systemctl preset %{name}.service >/dev/null 2>&1 || :

%preun

if [ $1 -eq 0 ]; then

    systemctl --no-reload disable %{name}.service >/dev/null 2>&1 || :
    systemctl stop %{name}.service >/dev/null 2>&1 || :
fi

%postun

systemctl daemon-reload >/dev/null 2>&1 || :

if [ $1 -eq 0 ]; then


    # rm -rf /var/log/%{name}
    # rm -rf /var/lib/%{name}
    

    # userdel %{name} 2>/dev/null || :
    # groupdel %{name} 2>/dev/null || :
    
    echo "Service %{name} has been removed."
fi

%files

%defattr(-,root,root,-)
%license LICENSE
%doc README.md


%attr(755,root,root) %{_bindir}/%{name}
%attr(644,root,root) /usr/share/%{name}/%{name}.jar


%config(noreplace) %attr(644,%{name},%{name}) %{_sysconfdir}/%{name}/application.properties

# Systemd service
%attr(644,root,root) %{_unitdir}/%{name}.service


%dir %attr(755,%{name},%{name}) /var/log/%{name}
%dir %attr(755,%{name},%{name}) /var/lib/%{name}
%dir %attr(755,root,root) %{_sysconfdir}/%{name}

%changelog
* Tue Dec 15 2023 Your Name <email@example.com> - 1.0.0-1
- Initial package
- Web service for analyzing damaged files
- Includes web interface and REST API