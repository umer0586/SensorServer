
# This script should only be invoke in SensorServer's root directory

import os
import shutil
import sys

# .../app/src/main/assets/webapp
deployment_dir = os.path.join(".","app","src","main","assets","webapp")
flutter_source_dir = os.path.join(".","sensors_dashboard")
flutter_web_build_dir = os.path.join(flutter_source_dir, "build","web")


def deploy():
    # Remove all files from sensors_test/build/web
    if os.path.exists(flutter_web_build_dir):
        print(f"cleaning: {flutter_web_build_dir}")
        shutil.rmtree(flutter_web_build_dir, ignore_errors=True)

    # Create deployment directory in Android's assets folder if doesn't exist 
    if not os.path.exists(deployment_dir):
        print(f"Creating deployment directory: {deployment_dir}")
        os.makedirs(deployment_dir)
    # If it exist (.../assets/webapp) then remove all file under that directory 
    else:
        print(f"Cleaning existing deployment directory: {deployment_dir}")
        shutil.rmtree(deployment_dir, ignore_errors=True)
        #os.makedirs(deployment_dir)

    # Build Flutter web app
    print("Building Flutter web app...")
    os.chdir("sensors_dashboard")
    
    build_command = f"flutter build web --web-renderer canvaskit"
    exit_code = os.system(build_command)

    # Check if build is successful
    if exit_code == 0:
        # Build is successfull, now move out from flutter source directory
        os.chdir("..")
    else:
        print(f"Failed : {build_command}", file=sys.stderr)
        sys.exit(1)    

    
    if os.path.exists(flutter_web_build_dir):
        print(f"Copying {flutter_web_build_dir} to {deployment_dir}")
        shutil.copytree(flutter_web_build_dir, deployment_dir, dirs_exist_ok=True)
        print("Deployment successful!")
    else:
        print(f"Error: web app was successfully built, but {flutter_web_build_dir} does not exist",file=sys.stderr)
        


deploy()
