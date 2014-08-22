package by.mcreader.imageloader.features;

public class ImageCombiner {

    private ImageCombiner() {
    }

    //    public void combineMultipleImages(ImageView imageView, int widthInPx, int heightInPx, String... partsUrls) {
//
//        StringBuilder cacheKey = new StringBuilder();
//
//        for (String part : partsUrls) {
//
//            cacheKey.append(part);
//
//        }
//
//        BitmapDrawable bitmapDrawable = TextUtils.isEmpty(cacheKey) ? new BitmapDrawable(mResources, mPlaceholderBitmap) : mImageCacher == null ? null : mImageCacher.getBitmapFromMemoryCache(cacheKey.toString());
//
//        if (bitmapDrawable != null) {
//
//            imageView.setImageDrawable(bitmapDrawable);
//
//        } else if ((cancelPotentialDownload(imageView, cacheKey.toString()))) {
//
//            CombineBitmapTask bitmapTask = new CombineBitmapTask(imageView);
//
//            AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(mResources, mPlaceholderBitmap, bitmapTask);
//
//            imageView.setImageDrawable(asyncBitmapDrawable);
//
//            bitmapTask.start(cacheKey.toString(), widthInPx, heightInPx, partsUrls);
//        }
//    }
//
//    public void combineMultipleImages(Context context, ImageView imageView, int widthInPx, int heightInPx, String... partsUrls) {
//
//        combineMultipleImages(imageView, Math.round(Converter.convertDpToPixel(context, widthInPx)), Math.round(Converter.convertDpToPixel(context, heightInPx)), partsUrls);
//
//    }

    // TODO: implement

//    public static BitmapDrawable combine(Context context, int widthInPx, int heightInPx, String... partsUrls) {
//
//        StringBuilder cacheKey = new StringBuilder();
//
//        for (String part : partsUrls) {
//
//            cacheKey.append(part);
//
//        }
//
//        BitmapDrawable bitmapDrawable = TextUtils.isEmpty(cacheKey) ? new BitmapDrawable(mResources, mPlaceholderBitmap) : mImageCacher == null ? null : mImageCacher.getBitmapFromMemoryCache(cacheKey.toString());
//
//        if (bitmapDrawable != null) {
//
////            imageView.setImageDrawable(bitmapDrawable);
//
//        } else if ((cancelPotentialDownload(imageView, cacheKey.toString()))) {
//
//            CombineBitmapTask bitmapTask = new CombineBitmapTask(imageView);
//
//            AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(mResources, mPlaceholderBitmap, bitmapTask);
//
//            imageView.setImageDrawable(asyncBitmapDrawable);
//
//            bitmapTask.start(cacheKey.toString(), widthInPx, heightInPx, partsUrls);
//        }
//
//    }
//
//    public class CombineBitmapTask extends SuperImageLoader.ImageAsyncTask {
//
//        private int DEFAULT_WIDTH = 300;
//        private int DEFAULT_HEIGHT = 300;
//
//        private int mWidth = DEFAULT_WIDTH;
//        private int mHeight = DEFAULT_HEIGHT;
//
//        private Map<String, float[]> positions;
//
//        private Canvas mCanvas;
//        private Bitmap mResultBitmap;
//
//        public CombineBitmapTask(ImageView imageView) {
//            super(imageView);
//        }
//
//        public void start(String cacheKey, int width, int height,
//                          String... partsUrls) {
//            mUrl = cacheKey;
//            if (width > 0) {
//                mWidth = width;
//            }
//            if (height > 0) {
//                mHeight = height;
//            }
//            executeOnExecutor(DUAL_THREAD_EXECUTOR, partsUrls);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            mResultBitmap = Bitmap.createBitmap(mWidth, mHeight,
//                    Bitmap.Config.ARGB_8888);
//            mCanvas = new Canvas(mResultBitmap);
//        }
//
//        @Override
//        protected BitmapDrawable doInBackground(String... params) {
//            positions = new HashMap<String, float[]>(params.length);
//            countOffsets(0, 0, mWidth, mHeight, params);
//            float[] offset = new float[4];
//            for (int i = 0; i < params.length; i++) {
//                synchronized (mPauseWorkLock) {
//                    while (mPauseWork && !isCancelled()) {
//                        try {
//                            mPauseWorkLock.wait();
//                        } catch (InterruptedException e) {
//                            // can be ignored
//                        }
//                    }
//                }
//                if (TextUtils.isEmpty(params[i])) {
//                    continue;
//                }
//                offset = positions.get(params[i]);
//                try {
//                    // if bitmap not cached, so loading it
//                    if (NetworkHelper.checkConnection(mContext)
//                            && !isCancelled() && getAttachedImageView() != null) {
//                        drawPart(mImageWorker.loadImage(params[i],
//                                (int) mWidth, (int) mHeight), offset);
//                    }
//                } catch (IOException e) {
//                    L.e(LOG_TAG, "can't load part to combine images in bitmap");
//                    return null;
//                } catch (Exception e) {
//                    L.e(LOG_TAG, "can't load part to combine images in bitmap");
//                    e.printStackTrace();
//                }
//            }
//            BitmapDrawable bitmapDrawable = null;
//            if (mResultBitmap != null) {
//                if (AndroidVersionsUtils.hasHoneycomb()
//                        && mResultBitmap.getConfig() != null) {
//                    bitmapDrawable = new BitmapDrawable(mResources,
//                            mResultBitmap);
//                } else {
//                    bitmapDrawable = new RecyclingBitmapDrawable(mResources,
//                            mResultBitmap);
//                }
//                mImageCacher.putBitmapToMemoryCache(mUrl, bitmapDrawable);
//            }
//            return bitmapDrawable;
//        }
//
//        private void drawPart(Bitmap part, float[] offset) throws Exception {
//            if (part != null && !part.isRecycled() && offset != null) {
//                part = Bitmap.createScaledBitmap(part,
//                        (int) Math.abs(offset[2] - offset[0]),
//                        (int) Math.abs(offset[3] - offset[1]), false);
//                mCanvas.drawBitmap(part, offset[0], offset[1], null);
//                part.recycle();
//                part = null;
//            }
//        }
//
//        private void countOffsets(float posX0, float posY0, float posX1,
//                                  float posY1, String[] urls) {
//            float width = Math.abs(posX1 - posX0);
//            float height = Math.abs(posY1 - posY0);
//            if (urls.length == 1) {
//                positions.put(urls[0],
//                        new float[]{posX0, posY0, posX1, posY1});
//                return; // the point of recursion exit
//            }
//            String[] splitedUrls1 = new String[urls.length / 2];
//            String[] splitedUrls2 = new String[urls.length
//                    - splitedUrls1.length];
//            System.arraycopy(urls, 0, splitedUrls1, 0, splitedUrls1.length);
//            System.arraycopy(urls, splitedUrls1.length, splitedUrls2, 0,
//                    splitedUrls2.length);
//            if (width >= height) {
//                countOffsets(posX0, posY0, posX0 + (width / 2), posY1,
//                        splitedUrls1);
//                countOffsets(posX0 + (width / 2), posY0, posX1, posY1,
//                        splitedUrls2);
//            } else {
//                countOffsets(posX0, posY0, posX1, posY0 + (height / 2),
//                        splitedUrls1);
//                countOffsets(posX0, posY0 + (height / 2), posX1, posY1,
//                        splitedUrls2);
//            }
//        }
//    }

}
