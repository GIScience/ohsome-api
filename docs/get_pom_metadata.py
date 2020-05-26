import xml.etree.ElementTree as xml

def pom():
    return xml.parse('../pom.xml')

def full_version():
    return pom().find('{http://maven.apache.org/POM/4.0.0}version').text

def major_version():
    return '.'.join(full_version().split('.')[:1])

def minor_version():
    return '.'.join(full_version().split('.')[:2])

def v_version(version):
    return "v{}".format(version)

def version_path():
    return v_version(major_version())

def project_name():
    return pom().find('{http://maven.apache.org/POM/4.0.0}name').text

if __name__ == '__main__':
    print("Path: {}".format(version_path()))
    print("Full: {}".format(full_version()))
    print("Major: {}".format(major_version()))
    print("Minor: {}".format(minor_version()))
