const frameImage = document.getElementById('processed-frame');
const fpsSpan = document.getElementById('fps');
const resolutionSpan = document.getElementById('resolution');

function refreshFrame() {
    fetch('/frame')
        .then(response => response.blob())
        .then(blob => {
            const objectURL = URL.createObjectURL(blob);
            frameImage.src = objectURL;
        });
}

// Mock data for FPS and resolution
fpsSpan.textContent = '15';
resolutionSpan.textContent = '640x480';

setInterval(refreshFrame, 100); // Refresh 10 times per second
